package org.project.backend.model;


public class OcrResult {
    private String text;

    public OcrResult() {}

    public OcrResult(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
