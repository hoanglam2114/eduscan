package fpt.eduscan.controller;

import fpt.eduscan.model.OCRResponse;
import fpt.eduscan.service.GeminiOCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private GeminiOCRService geminiOCRService;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_FILES = 5;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OCR Service is running!");
    }

    /**
     * Process single image
     */
    @PostMapping("/scan")
    public ResponseEntity<OCRResponse> scanImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(OCRResponse.error("File is empty"));
            }

            // Check file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                        .body(OCRResponse.error("File size exceeds 2MB limit"));
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(OCRResponse.error("File must be an image (JPG, PNG)"));
            }

            // Extract text using Gemini
            String extractedText = geminiOCRService.extractTextFromImage(file);

            // Return response
            OCRResponse response = OCRResponse.success(extractedText, file.getOriginalFilename());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(OCRResponse.error("Error processing image: " + e.getMessage()));
        }
    }

    /**
     * Process multiple images at once
     */
    @PostMapping("/scan-multiple")
    public ResponseEntity<List<OCRResponse>> scanMultipleImages(
            @RequestParam("files") MultipartFile[] files) {

        List<OCRResponse> responses = new ArrayList<>();

        try {
            // Validate that files array is not empty
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(List.of(OCRResponse.error("No files provided")));
            }

            // Validate max files limit
            if (files.length > MAX_FILES) {
                return ResponseEntity.badRequest()
                        .body(List.of(OCRResponse.error("Maximum " + MAX_FILES + " files allowed")));
            }

            System.out.println("Processing " + files.length + " files...");

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // Validate file type
                        String contentType = file.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                            responses.add(OCRResponse.error(
                                    "Invalid file type for: " + file.getOriginalFilename()));
                            continue;
                        }

                        // Validate file size
                        if (file.getSize() > MAX_FILE_SIZE) {
                            responses.add(OCRResponse.error(
                                    "File too large (max 2MB): " + file.getOriginalFilename()));
                            continue;
                        }

                        System.out.println("Processing: " + file.getOriginalFilename());

                        // Extract text
                        String extractedText = geminiOCRService.extractTextFromImage(file);
                        responses.add(OCRResponse.success(extractedText, file.getOriginalFilename()));

                        System.out.println("Success: " + file.getOriginalFilename());

                    } catch (Exception e) {
                        e.printStackTrace();
                        responses.add(OCRResponse.error(
                                "Error processing " + file.getOriginalFilename() + ": " + e.getMessage()));
                    }
                } else {
                    responses.add(OCRResponse.error("Empty file received"));
                }
            }

            System.out.println("Completed processing " + responses.size() + " files");
            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(OCRResponse.error("Error processing images: " + e.getMessage())));
        }
    }
}