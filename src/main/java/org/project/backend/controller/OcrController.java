package org.project.backend.controller;


import org.project.backend.model.OcrResult;
import org.project.backend.service.OcrService;
import org.project.backend.service.export.ExcelExportService;
import org.project.backend.service.export.PdfExportService;
import org.project.backend.service.export.WordExportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final OcrService ocrService;
    private final WordExportService wordService;
    private final ExcelExportService excelService;
    private final PdfExportService pdfService;

    public OcrController(OcrService ocrService,
                         WordExportService wordService,
                         ExcelExportService excelService,
                         PdfExportService pdfService) {
        this.ocrService = ocrService;
        this.wordService = wordService;
        this.excelService = excelService;
        this.pdfService = pdfService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String text = ocrService.extractText(file.getBytes());
        return ResponseEntity.ok(text);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam("format") String format) throws Exception {
        OcrResult result = ocrService.getLastResult();
        if (result == null || result.getText() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        byte[] data;
        String fileName;
        MediaType mediaType;

        switch (format.toLowerCase()) {
            case "word":
                data = wordService.export(result.getText());
                fileName = "ocr.docx";
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
                break;
            case "excel":
                data = excelService.export(result.getText());
                fileName = "ocr.xlsx";
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
                break;
            case "pdf":
                data = pdfService.export(result.getText());
                fileName = "ocr.pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            default:
                return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(data);
    }
}
