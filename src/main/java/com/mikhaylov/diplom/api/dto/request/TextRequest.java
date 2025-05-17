package com.mikhaylov.diplom.api.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TextRequest extends BaseRequest {
    private String text;
    private boolean saveHistory;
}
