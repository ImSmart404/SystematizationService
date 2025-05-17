package com.mikhaylov.diplom.api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Базовый класс для всех ответов от сервера.
 * Содержит общие поля успеха операции и сообщения.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseResponse {
    private String requestId;
    private boolean success;
    private String message;
}
