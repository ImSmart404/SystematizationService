package com.mikhaylov.diplom.api.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Ответ с очищенным текстом после парсинга и очистки HTML.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class CleanTextResponse extends BaseResponse {
    private String cleanedText;
}
