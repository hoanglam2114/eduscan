package org.project.backend.controller;

import org.project.backend.model.ExportResult;
import org.project.backend.model.OcrResult;
import org.project.backend.service.ExportService;
import org.project.backend.service.LlmService;
import org.project.backend.service.UsageLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/assist")
@CrossOrigin(origins = "*") // Cho phép CORS từ frontend
public class WritingAssistantController {

    private static final Logger log = LoggerFactory.getLogger(OcrController.class);

    private final UsageLimitingService usageLimitingService;
    private final ExportService exportService;
    private final LlmService llmService;

    @Autowired
    public WritingAssistantController(LlmService llmService, ExportService exportService, UsageLimitingService usageLimitingService) {
        this.llmService = llmService;
        this.exportService = exportService;
        this.usageLimitingService = usageLimitingService; // Inject service mới
    }

    /**
     * API endpoint để tạo nội dung từ prompt
     * POST /api/assist/generate?provider=geminiLlm
     * Body: { "prompt": "Viết email chuyên nghiệp về..." }
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateContent(
            @RequestBody Map<String, String> requestBody,
            @RequestParam(value = "provider", defaultValue = "geminiLlm") String provider) {

        String prompt = requestBody.get("prompt");

        // Validate input
        if (prompt == null || prompt.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Yêu cầu (prompt) không được để trống.");
            errorResponse.put("status", "error");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
//            String generatedText = llmService.generateContent(prompt, provider);

            // Tạo response object
//            Map<String, Object> response = new HashMap<>();
//            response.put("status", "success");
//            response.put("content", generatedText);
//            response.put("provider", provider);
//            response.put("timestamp", System.currentTimeMillis());

            String content = llmService.generateContent(prompt, provider);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
                    .body(content);


        } catch (IllegalArgumentException e) {
            // Provider không tồn tại
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            // Lỗi server hoặc API
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo nội dung: " + e.getMessage());
            errorResponse.put("status", "error");

            // Log lỗi
            System.err.println("Error generating content: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
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
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportResult.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_DISPOSITION)
                    .body(exportResult.getData());

        } catch (IllegalArgumentException e) {
            log.error("EXPORT FAILED - UNSUPPORTED FORMAT: {}", format, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {
            log.error("EXPORT FAILED: ", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * API endpoint để lấy danh sách providers có sẵn
     * GET /api/assist/providers
     */
    @GetMapping("/providers")
    public ResponseEntity<?> getAvailableProviders() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("providers", llmService.getAvailableProviders().keySet());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi lấy danh sách providers: " + e.getMessage());
            errorResponse.put("status", "error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     * GET /api/assist/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Writing Assistant");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}