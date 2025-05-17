package com.mikhaylov.diplom.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Getter
@Component
public class NLPModelProvider {
    private DictionaryLemmatizer lemmatizer;
    private POSTaggerME posTagger;

    @PostConstruct
    public void init() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            // POS model
            try (InputStream posModelIn = classLoader.getResourceAsStream("model/en-pos-maxent.bin")) {
                if (posModelIn == null) throw new RuntimeException("POS model not found");
                POSModel posModel = new POSModel(posModelIn);
                posTagger = new POSTaggerME(posModel);
            }

            // Lemmatizer dictionary
            try (InputStream dictLemmatizer = classLoader.getResourceAsStream("model/en-lemmatizer.dict")) {
                if (dictLemmatizer == null) throw new RuntimeException("Lemmatizer dict not found");
                lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации NLP моделей: " + e.getMessage(), e);
        }
    }
}
