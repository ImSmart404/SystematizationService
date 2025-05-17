package com.mikhaylov.diplom.service.impl;

import com.mikhaylov.diplom.exception.TextProcessingException;
import com.mikhaylov.diplom.service.NLPModelProvider;
import com.mikhaylov.diplom.service.TextCleaningService;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TextCleaningServiceImpl implements TextCleaningService {

    private final NLPModelProvider nlpModelProvider;

    public TextCleaningServiceImpl(NLPModelProvider nlpModelProvider) {
        this.nlpModelProvider = nlpModelProvider;
    }

    public String cleanText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            throw new TextProcessingException("Text is empty or null");
        }

        String cleanText = Jsoup.parse(rawText).text();

        cleanText = cleanText.replaceAll("http\\S+", "");
        cleanText = cleanText.replaceAll("[\\p{So}\\p{Cn}]", "");

        cleanText = cleanText.toLowerCase();

        return cleanText;
    }

    public List<String> lemmatizeText(String rawText) {
        String cleanText = cleanText(rawText);
        List<String> words = tokenize(cleanText);
        List<String> normalizedWords;
        try {
            normalizedWords = lemmatize(words);
        } catch (Exception e) {
            throw new TextProcessingException("Error occured while lemmatizing text: " + e.getMessage());
        }

        // Возвращаем полный список лемм (с повторами), но убираем пустые и "_" (неизвестные)

        return normalizedWords.stream()
                .filter(lemma -> lemma != null && !lemma.isEmpty() && !lemma.equals("_"))
                .toList();
    }

    public List<String> getUniqueLemmas(List<String> lemmas) {
        Set<String> unique = new LinkedHashSet<>(lemmas);
        return new ArrayList<>(unique);
    }

    private List<String> tokenize(String text) {
        String cleanText = cleanText(text);
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        return List.of(tokenizer.tokenize(cleanText));
    }

    private List<String> lemmatize(List<String> words) {
        POSTaggerME tagger = nlpModelProvider.getPosTagger();
        DictionaryLemmatizer lemmatizer = nlpModelProvider.getLemmatizer();

        String[] wordArray = words.toArray(new String[0]);
        String[] posTags = tagger.tag(wordArray);
        String[] lemmas = lemmatizer.lemmatize(wordArray, posTags);

        List<String> result = new ArrayList<>();
        for (String lemma : lemmas) {
            result.add(lemma.equals("O") ? "_" : lemma);
        }
        return result;
    }

    public int countSentences(String text) {
        if (text == null || text.isEmpty()) return 0;
        String[] sentences = text.split("[.!?]+");
        return (int) Arrays.stream(sentences).filter(s -> !s.trim().isEmpty()).count();
    }

    // Подсчет частоты слов
    public Map<String, Integer> countWordFrequencies(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words) {
            if (word == null || word.isEmpty() || word.equals("_")) continue;
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        return freq;
    }

    public int countConjunctions(String text) {
        if (text == null || text.isEmpty()) return 0;
        Set<String> conjunctions = Set.of(
                "and", "but", "or", "nor", "for", "yet", "so",
                "although", "because", "since", "unless", "while",
                "if", "though", "whereas", "whether", "as", "after", "before", "until", "once", "when"
        );        String[] tokens = text.toLowerCase().split("\\W+");
        int count = 0;
        for (String token : tokens) {
            if (conjunctions.contains(token)) count++;
        }
        return count;
    }

}
