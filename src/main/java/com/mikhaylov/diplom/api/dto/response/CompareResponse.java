package com.mikhaylov.diplom.api.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Ответ с результатом сравнения двух текстов.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class CompareResponse extends BaseResponse {
    private Integer levenshteinDistance;
    private Double jaccardSimilarity;
    private Integer text1Length;
    private Integer text2Length;
    private String detailedReport;
}
