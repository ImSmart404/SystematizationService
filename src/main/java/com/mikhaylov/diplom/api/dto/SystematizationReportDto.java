package com.mikhaylov.diplom.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Хранит отчет по систематизации текстов
 */
@Data
@Builder
public class SystematizationReportDto {

    private int sentenceCount;
    private double averageSentenceLength;
    private int uniqueWordCount;
    private Map<String, Double> topKeywordsDensity;
    private double cohesionCoefficient;

    private String summaryText;
}