package fpt.eduscan.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OCRResponse {
    // Getters and Setters
    private String extractedText;
    private String fileName;
    private boolean success;
    private String error;

    public OCRResponse() {}

    public OCRResponse(String extractedText, String fileName, boolean success) {
        this.extractedText = extractedText;
        this.fileName = fileName;
        this.success = success;
    }

    public static OCRResponse success(String text, String fileName) {
        return new OCRResponse(text, fileName, true);
    }

    public static OCRResponse error(String errorMsg) {
        OCRResponse response = new OCRResponse();
        response.success = false;
        response.error = errorMsg;
        return response;
    }

}