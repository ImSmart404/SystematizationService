package com.mikhaylov.diplom.api.dto.request;

import lombok.Data;

/**
 * Базовый класс для всех DTO-запросов в системе.
 * Содержит общий параметр запроса — requestId,
 * который используется для отслеживания обращений.
 * Это абстрактный класс, от которого должны наследоваться
 * все классы входных данных.
 */
@Data
public abstract class BaseRequest {
    private String requestId;
}
