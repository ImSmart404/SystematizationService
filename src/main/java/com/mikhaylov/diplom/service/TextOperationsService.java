package com.mikhaylov.diplom.service;

import com.mikhaylov.diplom.api.dto.SystematizationReportDto;
import com.mikhaylov.diplom.api.dto.request.CompareRequest;
import com.mikhaylov.diplom.api.dto.response.CompareResponse;

public interface TextOperationsService {

    String fetchAndCleanText(String url, boolean saveHistory);

    String cleanText(String rawText);

    CompareResponse compareTexts(CompareRequest compareRequest);

    String fetchTextFromUrl(String url);

    SystematizationReportDto analyzeAndSystematize(String text);

}
