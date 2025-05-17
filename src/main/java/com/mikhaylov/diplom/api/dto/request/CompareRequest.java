package com.mikhaylov.diplom.api.dto.request;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO-класс для запроса на сравнение двух текстов.
 * Используется в API, где требуется оценить степень сходства
 * между двумя строками с помощью выбранной метрики (например,
 * Левенштейна, Жаккара, Хэмминга и др.).
 * Поля:
 * - text1: первый текст для сравнения.
 * - text2: второй текст для сравнения.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompareRequest extends BaseRequest {
    private String text1;
    private String text2;
}