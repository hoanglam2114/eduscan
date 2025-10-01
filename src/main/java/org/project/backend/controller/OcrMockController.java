package org.project.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/ocr")
public class OcrMockController {

    @PostMapping("/upload")
    public ResponseEntity<byte[]> mockOcrUpload() throws Exception {
        // Lấy file mẫu từ resources (src/main/resources/static/)
        InputStream inputStream = getClass().getResourceAsStream("/static/sample_word.docx");

        if (inputStream == null) {
            throw new RuntimeException("Không tìm thấy file mẫu sample_word.docx trong resources/static/");
        }

        byte[] fileBytes = inputStream.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ocr_result.docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportOcrFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) throws Exception {

        // ---- Giả lập OCR, trả về file tĩnh ----
        String resourceFile;
        String contentType;
        String fileName;

        switch (format.toLowerCase()) {
            case "pdf":
                resourceFile = "/static/sample.pdf";
                contentType = "application/pdf";
                fileName = "ocr_result.pdf";
                break;
            case "xlsx":
                resourceFile = "/static/sample_excel.xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileName = "ocr_result.xlsx";
                break;
            case "docx":
            default:
                resourceFile = "/static/sample_word.docx";
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                fileName = "ocr_result.docx";
                break;
        }

        InputStream inputStream = getClass().getResourceAsStream(resourceFile);
        if (inputStream == null) {
            throw new RuntimeException("Không tìm thấy file mẫu " + resourceFile);
        }

        byte[] fileBytes = inputStream.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileBytes);
    }
}
