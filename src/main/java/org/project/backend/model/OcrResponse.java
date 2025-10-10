package org.project.backend.model;

public record OcrResponse(String fileName, String text, String error) {
    public static OcrResponse success(String fileName, String text) {
        return new OcrResponse(fileName, text, null);
    }

    public static OcrResponse failure(String fileName, String error) {
        return new OcrResponse(fileName, null, error);
    }
}