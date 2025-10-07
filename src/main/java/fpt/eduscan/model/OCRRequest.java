package fpt.eduscan.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OCRRequest {
    private String imageBase64;
    private String fileName;

    public OCRRequest() {}

    public OCRRequest(String imageBase64, String fileName) {
        this.imageBase64 = imageBase64;
        this.fileName = fileName;
    }
}