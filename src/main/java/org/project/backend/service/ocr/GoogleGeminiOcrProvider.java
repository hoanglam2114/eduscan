package org.project.backend.service.ocr;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

// 1. Biến class này thành một Spring Component để @Value hoạt động
@Component("gemini")
public class GoogleGeminiOcrProvider implements OcrProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleGeminiOcrProvider.class);

    // 2. Spring sẽ inject key từ application.properties vào đây
    @Value("${api.google.gemini.key}")
    private String apiKey;

    // 3. Sửa lại tên model cho đúng. Model hiện tại là "gemini-1.5-flash"
    private static final String MODEL_NAME = "gemini-2.5-flash-lite"; // Hoặc "gemini-1.5-flash" nếu bạn muốn dùng model đó
    private static final String API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    @Override
    public String extractText(byte[] imageBytes, String mimeType) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();

        // 4. URL được xây dựng ở đây, sau khi `apiKey` đã chắc chắn có giá trị
        String apiUrl = String.format(API_URL_TEMPLATE, MODEL_NAME, apiKey);

        // ... code còn lại không đổi ...
        JSONObject payload = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", "Trích xuất toàn bộ văn bản trong ảnh này. Chỉ trả về văn bản thô, không thêm bất kỳ định dạng hay giải thích nào."))
                                        .put(new JSONObject()
                                                .put("inlineData", new JSONObject()
                                                        .put("mimeType", mimeType)
                                                        .put("data", Base64.getEncoder().encodeToString(imageBytes))
                                                )
                                        )
                                )
                        )
                );

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(apiUrl).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;

            if (responseBody == null) {
                throw new IOException("Received empty response body from API.");
            }

            if (!response.isSuccessful()) {
                log.error("LỖI KHI GỌI GEMINI API - Status: {}, Body: {}", response.code(), responseBody);
                throw new IOException("Gemini API Error: " + responseBody);
            }

            JSONObject result = new JSONObject(responseBody);

            if (result.has("candidates")) {
                JSONArray candidates = result.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject content = firstCandidate.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts.length() > 0 && parts.getJSONObject(0).has("text")) {
                        return parts.getJSONObject(0).getString("text");
                    }
                }
            }

            return "No text or candidates found in the response.";
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }
}