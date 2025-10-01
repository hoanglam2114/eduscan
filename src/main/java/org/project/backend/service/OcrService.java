package org.project.backend.service;

import org.project.backend.dto.OcrResult;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {
    OcrResult extractText(MultipartFile file);
}
