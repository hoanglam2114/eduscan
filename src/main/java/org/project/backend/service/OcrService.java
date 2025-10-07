package org.project.backend.service;

import org.project.backend.model.OcrResult;
import org.project.backend.service.ocr.OcrProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OcrService {
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