package com.mikhaylov.diplom.api.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UrlRequest extends BaseRequest {
    private String url;
    private boolean saveHistory;
}
