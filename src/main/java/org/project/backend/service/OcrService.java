package org.project.backend.service;

import org.project.backend.controller.OcrController;
import org.project.backend.model.OcrResponse;
import org.project.backend.model.OcrResult;
import org.project.backend.service.ocr.OcrProvider;
import org.project.backend.util.ImagePreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);


    private OcrResult lastResult;
    private final Map<String, OcrProvider> providers;

    // Spring sẽ tự động inject tất cả các bean OcrProvider vào một Map
    // với key là tên của bean (ví dụ: "vision", "gemini")
    public OcrService(List<OcrProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(OcrProvider::getProviderName, Function.identity()));
    }

    public String extractText(byte[] image, String mimeType, String providerName) throws Exception {
        OcrProvider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported OCR provider: " + providerName);
        }
        String text = provider.extractText(image, mimeType);
        lastResult = new OcrResult(text);
        return text;
    }

    public OcrResult getLastResult() {
        return lastResult;
    }

    public List<OcrResponse> extractTextFromMultipleFiles(MultipartFile[] files, String providerName) {
        OcrProvider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported OCR provider: " + providerName);
        }

        return Arrays.stream(files)
                .parallel() // Xử lý song song
                .map(file -> {
                    try {
                        // Tiền xử lý ảnh
                        byte[] processedBytes = ImagePreprocessor.binarize(file.getBytes());
                        // Gửi ảnh đã xử lý đi (dưới dạng png)
                        String text = provider.extractText(processedBytes, "image/png");
                        return OcrResponse.success(file.getOriginalFilename(), text);
                    } catch (Exception e) {
                        log.error("Lỗi xử lý file {}: {}", file.getOriginalFilename(), e.getMessage());
                        return OcrResponse.failure(file.getOriginalFilename(), e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    public List<String> getAvailableProviders() {
        return providers.keySet().stream().collect(Collectors.toList());
    }

    /**
     * PHIÊN BẢN TEST: Không gọi API thật, chỉ trả về một chuỗi giả lập.
     * Phương thức này sẽ nhận file ảnh nhưng chỉ trả về một thông báo xác nhận.
     */
    public String extractText(byte[] image) throws Exception {
        // Bỏ qua việc gọi API OCR thật
        // String text = VisionClient.extractTextFromImage(image);
        // String text = GeminiOCRClient.extractTextFromImage(image, mimeType);

        // Thay vào đó, trả về một chuỗi cố định để test
        String mockText = "Backend đã nhận được file thành công. Kích thước file: "
                + image.length + " bytes. Đây là phản hồi giả lập để test kết nối.";

        // Lưu kết quả giả lập này để có thể test cả chức năng export
        lastResult = new OcrResult(mockText);
        return mockText;
    }
}