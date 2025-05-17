package com.mikhaylov.diplom.api.controller;

import com.mikhaylov.diplom.api.dto.SystematizationReportDto;
import com.mikhaylov.diplom.api.dto.request.CompareRequest;
import com.mikhaylov.diplom.api.dto.request.TextRequest;
import com.mikhaylov.diplom.api.dto.request.UrlRequest;
import com.mikhaylov.diplom.api.dto.response.CleanTextResponse;
import com.mikhaylov.diplom.api.dto.response.CompareResponse;
import com.mikhaylov.diplom.service.TextOperationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/textOperations")
@Slf4j
public class TextOperationsController {

    private final TextOperationsService operationsService;

    public TextOperationsController(TextOperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @PostMapping("/extractAndClean")
    @ResponseBody
    public ResponseEntity<CleanTextResponse> extractAndClean(@RequestBody UrlRequest urlRequest) {
        log.info("Получен запрос на очистку текста. rqId={}, url={}, saveHistory={}",
                urlRequest.getRequestId(), urlRequest.getUrl(), urlRequest.isSaveHistory());
        String cleaned = operationsService.fetchAndCleanText(urlRequest.getUrl(), urlRequest.isSaveHistory());
        CleanTextResponse resp = CleanTextResponse
                .builder()
                .requestId(urlRequest.getRequestId())
                .success(true)
                .message("OK")
                .cleanedText(cleaned)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/systematizeFromUrl")
    @ResponseBody
    public ResponseEntity<SystematizationReportDto> systematizeFromUrl(@RequestBody UrlRequest urlRequest) {
        String text = operationsService.fetchTextFromUrl(urlRequest.getUrl());
        SystematizationReportDto report = operationsService.analyzeAndSystematize(text);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/systematizeFromText")
    @ResponseBody
    public ResponseEntity<SystematizationReportDto> systematizeFromText(@RequestBody TextRequest textRequest) {
        String text = operationsService.cleanText(textRequest.getText());
        SystematizationReportDto report = operationsService.analyzeAndSystematize(text);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/compare")
    @ResponseBody
    public ResponseEntity<CompareResponse> compareTexts(@RequestBody CompareRequest request) {
        log.info("Получен запрос на сравнение текстов, requestId = {}", request.getRequestId());

        CompareResponse resp = operationsService.compareTexts(request);
        return ResponseEntity.ok(resp);
    }
}
