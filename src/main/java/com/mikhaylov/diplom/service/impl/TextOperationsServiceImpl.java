package com.mikhaylov.diplom.service.impl;

import com.mikhaylov.diplom.api.dto.SystematizationReportDto;
import com.mikhaylov.diplom.api.dto.request.CompareRequest;
import com.mikhaylov.diplom.api.dto.response.CompareResponse;
import com.mikhaylov.diplom.exception.TextProcessingException;
import com.mikhaylov.diplom.repository.CleanTextRepository;
import com.mikhaylov.diplom.repository.RawTextRepository;
import com.mikhaylov.diplom.repository.TextComparisonRepository;
import com.mikhaylov.diplom.repository.entity.CleanTextEntity;
import com.mikhaylov.diplom.repository.entity.RawTextEntity;
import com.mikhaylov.diplom.repository.entity.TextComparisonEntity;
import com.mikhaylov.diplom.service.TextCleaningService;
import com.mikhaylov.diplom.service.TextOperationsService;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Класс содержит основную логику сервиса
 */
@Service
public class TextOperationsServiceImpl implements TextOperationsService {

    private final TextCleaningService textCleaningService;
    private final RawTextRepository rawTextRepository;
    private final CleanTextRepository cleanTextRepository;
    private final TextComparisonRepository textComparisonRepository;

    public TextOperationsServiceImpl(TextCleaningService textCleaningService,
                                     RawTextRepository rawTextRepository,
                                     CleanTextRepository cleanTextRepository,
                                     TextComparisonRepository textComparisonRepository) {
        this.textCleaningService = textCleaningService;
        this.rawTextRepository = rawTextRepository;
        this.cleanTextRepository = cleanTextRepository;
        this.textComparisonRepository = textComparisonRepository;
    }

    /**
     * Скачивает HTML по URL и очищает текст от тегов и мусора.
     */
    @Transactional
    public String fetchAndCleanText(String url, boolean saveHistory) {
        Optional<RawTextEntity> optionalRaw = rawTextRepository.findFirstBySourceUrl(url);

        RawTextEntity rawTextEntity = optionalRaw.orElseGet(() -> {
            String parsedText = fetchTextFromUrl(url);
            if (parsedText == null || parsedText.isEmpty()) {
                throw new TextProcessingException("Не удалось получить текст с сайта: " + url);
            }
            RawTextEntity raw = RawTextEntity.builder()
                    .sourceUrl(url)
                    .rawText(parsedText)
                    .build();
            if (saveHistory) {
                return rawTextRepository.save(raw);
            }
            return raw;
        });

        Optional<CleanTextEntity> optionalClean = cleanTextRepository.findByRawText(rawTextEntity);

        if (optionalClean.isPresent()) {
            return optionalClean.get().getCleanedText();
        } else {
            // Чистим и сохраняем
            String cleaned = textCleaningService.cleanText(rawTextEntity.getRawText());
            CleanTextEntity cleanTextEntity = CleanTextEntity.builder()
                    .rawText(rawTextEntity)
                    .cleanedText(cleaned)
                    .build();
            if (saveHistory) {
                cleanTextRepository.save(cleanTextEntity);
            }
            return cleaned;
        }
    }

    /**
     * Возвращает очищенный текст, если пользователь прислал его напрямую.
     */
    public String cleanText(String rawText) {
        // Очистка текста от спецсимволов, html и тп
        return textCleaningService.cleanText(rawText);
    }

    /**
     * Сравнивает два текста и возвращает метрики сравнения.
     */
    public CompareResponse compareTexts(CompareRequest compareRequest) {
        String text1 = compareRequest.getText1();
        String text2 = compareRequest.getText2();

        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            throw new TextProcessingException("Оба текста должны быть непустыми");
        }

        List<String> lemmas1 = textCleaningService.lemmatizeText(text1);
        List<String> lemmas2 = textCleaningService.lemmatizeText(text2);

        String lemmasStr1 = String.join(" ", lemmas1);
        String lemmasStr2 = String.join(" ", lemmas2);

        int levenshteinDistance = levenshteinDistance(lemmasStr1, lemmasStr2);

        Set<String> set1 = new HashSet<>(lemmas1);
        Set<String> set2 = new HashSet<>(lemmas2);
        double jaccardSimilarity = jaccardCoefficient(set1, set2);

        TextComparisonEntity comparison = new TextComparisonEntity();
        comparison.setCleanText1(findCleanTextEntityByRawTextString(compareRequest.getText1()));
        comparison.setCleanText2(findCleanTextEntityByRawTextString(compareRequest.getText2()));
        comparison.setSimilarityScore(jaccardSimilarity);
        comparison.setDetailedReport(createDetailedReport(levenshteinDistance, jaccardSimilarity));
        comparison.setCreatedAt(LocalDateTime.now());

        textComparisonRepository.save(comparison);

        return CompareResponse.builder()
                .levenshteinDistance(levenshteinDistance)
                .jaccardSimilarity(jaccardSimilarity)
                .text1Length(lemmas1.size())
                .text2Length(lemmas2.size())
                .detailedReport(createDetailedReport(levenshteinDistance, jaccardSimilarity))
                .build();
    }

    public String fetchTextFromUrl(String url) {
        // Ищем запись с таким URL
        Optional<RawTextEntity> optionalRaw = rawTextRepository.findFirstBySourceUrl(url);

        if (optionalRaw.isPresent()) {
            return optionalRaw.get().getRawText();
        } else {
            // Парсим сайт реально
            Document doc;
            try {
                doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(5000)
                        .get();

            } catch (Exception e) {
                throw new TextProcessingException("Can not fetch text from url with exception: " + e.getMessage());
            }

            // Сохраняем в базу
            RawTextEntity newRawText = RawTextEntity.builder()
                    .sourceUrl(url)
                    .rawText(doc.body().text())
                    .build();

            rawTextRepository.save(newRawText);

            return newRawText.getRawText();
        }
    }

    /**
     * Систематизирует текст — считает метрики связности, структуру и тп.
     */
    public SystematizationReportDto analyzeAndSystematize(String text) {
        if (text == null || text.isEmpty()) {
            throw new TextProcessingException("Get null or empty text");
        }

        // Получаем полный список лемм с повторами
        List<String> lemmas = textCleaningService.lemmatizeText(text);

        // Кол-во предложений
        int sentenceCount = textCleaningService.countSentences(text);

        // Средняя длина предложения (слов)
        double avgSentenceLength = sentenceCount == 0 ? 0 : (double) lemmas.size() / sentenceCount;

        // Уникальные леммы (для подсчёта уникальных слов)
        List<String> uniqueLemmas = textCleaningService.getUniqueLemmas(lemmas);

        // Частоты слов
        Map<String, Integer> freqMap = textCleaningService.countWordFrequencies(lemmas);

        // Топ 5 слов по частоте
        List<Map.Entry<String, Integer>> topWords = freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();

        // Плотность ключевых слов = частота / общее кол-во слов
        Map<String, Double> keywordDensity = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : topWords) {
            keywordDensity.put(entry.getKey(), entry.getValue() / (double) lemmas.size());
        }

        // Связность (кол-во союзов / кол-во предложений)
        int conjunctionCount = textCleaningService.countConjunctions(text);
        double cohesion = sentenceCount == 0 ? 0 : (double) conjunctionCount / sentenceCount;

        return SystematizationReportDto.builder()
                .sentenceCount(sentenceCount)
                .averageSentenceLength(avgSentenceLength)
                .uniqueWordCount(uniqueLemmas.size())
                .topKeywordsDensity(keywordDensity)
                .cohesionCoefficient(cohesion)
                .build();
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    // Жаккар (пересечение / объединение)
    private double jaccardCoefficient(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private CleanTextEntity findCleanTextEntityByRawTextString(String rawTextString) {
        Optional<RawTextEntity> optRaw = rawTextRepository.findByRawText(rawTextString);
        RawTextEntity rawTextEntity = optRaw.orElseGet(() -> {
            RawTextEntity newRaw = new RawTextEntity();
            newRaw.setRawText(rawTextString);
            return rawTextRepository.save(newRaw);
        });

        // Теперь ищем CleanTextEntity по rawTextEntity
        Optional<CleanTextEntity> optClean = cleanTextRepository.findByRawText(rawTextEntity);
        if (optClean.isPresent()) {
            return optClean.get();
        } else {
            CleanTextEntity clean = new CleanTextEntity();
            clean.setRawText(rawTextEntity);
            clean.setCleanedText(textCleaningService.cleanText(rawTextString));
            clean.setCreatedAt(LocalDateTime.now());
            return cleanTextRepository.save(clean);
        }
    }

    private String createDetailedReport(int levenshteinDistance, double jaccardSimilarity) {

        return "Отчёт по сравнению текстов:\n" + String.format("- Расстояние Левенштейна: %d (%s)\n", levenshteinDistance,
                levenshteinDistance == 0 ? "Тексты идентичны" :
                        levenshteinDistance < 10 ? "Очень похожи" :
                                levenshteinDistance < 30 ? "Среднее сходство" :
                                        "Сильно отличаются") +

                // Жаккар — от 0 до 1, где 1 — полное совпадение
                String.format("- Коэффициент Жаккара: %.4f (%s)\n", jaccardSimilarity,
                        jaccardSimilarity > 0.75 ? "Высокая схожесть" :
                                jaccardSimilarity > 0.4 ? "Средняя схожесть" :
                                        "Низкая схожесть");
    }
}
