package org.project.backend.service;

import org.springframework.web.multipart.MultipartFile;


import org.project.backend.config.GeminiClient;
import org.project.backend.model.OcrResult;
import org.springframework.stereotype.Service;

@Service
public class OcrService {
    private OcrResult lastResult;

    public String extractText(byte[] image) throws Exception {
        String text = GeminiClient.extractTextFromImage(image);
        lastResult = new OcrResult(text);
        return text;
    }

    public OcrResult getLastResult() {
        return lastResult;
    }
}

