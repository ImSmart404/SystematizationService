package com.mikhaylov.diplom.service;

import java.util.List;
import java.util.Map;

public interface TextCleaningService {
    /**
     * Очищает текст от HTML, спецсимволов и прочего мусора
     * @param rawText исходный текст с HTML и прочим
     * @return очищенный, чистый текст
     */
    String cleanText(String rawText);

    List<String> lemmatizeText(String rawText);

    int countSentences(String text);

    Map<String, Integer> countWordFrequencies(List<String> words);

    int countConjunctions(String text);

    List<String> getUniqueLemmas(List<String> lemmas);
}