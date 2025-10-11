package org.project.backend.controller;

import org.project.backend.model.ExportResult;
import org.project.backend.model.OcrResponse;
import org.project.backend.model.OcrResult;
import org.project.backend.service.ExportService;
import org.project.backend.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@CrossOrigin
@RestController
@RequestMapping("/api")
public class OcrController {

    private static final Logger log = LoggerFactory.getLogger(OcrController.class);


    private final OcrService ocrService;
    private final ExportService exportService;

    public OcrController(OcrService ocrService, ExportService exportService) {
        this.ocrService = ocrService;
        this.exportService = exportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndExtract(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "provider", defaultValue = "gemini") String provider) {

        if (files.length > 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chỉ được phép upload tối đa 10 file.");
        }

        try {
            List<OcrResponse> results = ocrService.extractTextFromMultipleFiles(files, provider);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("OCR UPLOAD FAILED: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Xảy ra lỗi trong quá trình xử lý file: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody OcrResult result, @RequestParam("format") String format) {
        try {
            if (result == null || result.getText() == null) {
                return ResponseEntity.badRequest().build();
            }
            ExportResult exportResult = exportService.export(result.getText(), format);
            return ResponseEntity.ok()
                    .contentType(exportResult.getMediaType())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportResult.getFileName() + "\"")
                    .body(exportResult.getData());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/providers")
    public ResponseEntity<List<String>> getOcrProviders() {
        return ResponseEntity.ok(ocrService.getAvailableProviders());
    }

    @GetMapping("/formats")
    public ResponseEntity<List<String>> getExportFormats() {
        return ResponseEntity.ok(exportService.getAvailableFormats());
    }
}