package org.project.backend.controller;

import org.project.backend.model.ExportResult;
import org.project.backend.model.OcrResponse;
import org.project.backend.model.OcrResult;
import org.project.backend.service.ExportService;
import org.project.backend.service.OcrService;
import org.project.backend.service.UsageLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.project.backend.service.UsageLimitingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class OcrController {

    private static final Logger log = LoggerFactory.getLogger(OcrController.class);

    private final UsageLimitingService usageLimitingService;
    private final OcrService ocrService;
    private final ExportService exportService;

    public OcrController(OcrService ocrService, ExportService exportService,UsageLimitingService usageLimitingService) {
        this.ocrService = ocrService;
        this.exportService = exportService;
        this.usageLimitingService = usageLimitingService; // Inject service mới
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndExtract(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "provider", defaultValue = "gemini") String provider,
    HttpServletRequest request)
        { // Thêm HttpServletRequest
            // --- BẮT ĐẦU LOGIC GIỚI HẠN ---
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra nếu người dùng chưa đăng nhập (anonymous)
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                String clientIp = usageLimitingService.getClientIp(request);
                if (!usageLimitingService.isAllowed(clientIp)) {
                    // Trả về lỗi 429 nếu vượt quá giới hạn
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body("Bạn đã hết số lần sử dụng miễn phí. Vui lòng đăng nhập để tiếp tục.");
                }
            }

        // --- KẾT THÚC LOGIC GIỚI HẠN ---

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