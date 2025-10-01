package org.project.backend.controller;

import lombok.RequiredArgsConstructor;
import org.project.backend.dto.OcrResult;
import org.project.backend.service.DocumentService;
import org.project.backend.service.OcrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;
    private final DocumentService documentService;

    @PostMapping("/scan")
    public ResponseEntity<byte[]> scanImage(@RequestParam("image") MultipartFile file) {
        OcrResult result = ocrService.extractText(file);

        byte[] docx = documentService.createDocx(result.getText());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scan.docx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docx);
    }
}

