package org.project.backend.service.ocr;

//import com.itextpdf.text.log.Logger;
//import com.itextpdf.text.log.LoggerFactory;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

import org.slf4j.Logger; // Thêm import này
import org.slf4j.LoggerFactory; // Thêm import này

@Component("gemini")
public class GoogleGeminiOcrProvider implements OcrProvider{

    // Thêm một đối tượng Logger
    private static final Logger log = LoggerFactory.getLogger(GoogleGeminiOcrProvider.class);


    // THAY THẾ KHÓA API NÀY BẰNG API KEY CỦA BẠN LẤY TỪ GOOGLE AI STUDIO HOẶC GCP
    @Value("${api.google.gemini.key}") // Lấy API key từ application.properties
    private String API_KEY ;

    // Sử dụng model Gemini-2.5 Flash, được tối ưu cho tốc độ và tác vụ multimodal
    private static final String MODEL_NAME = "veo-2.0-generate-001";
    private String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent" + "?key=" + API_KEY;

    /**
     * Trích xuất văn bản từ hình ảnh sử dụng Google Gemini API.
     * @param imageBytes Mảng byte của hình ảnh đầu vào.
     * @param mimeType MIME type của hình ảnh (ví dụ: "image/jpeg", "image/png").
     * @return Văn bản đã trích xuất từ hình ảnh.
     * @throws IOException Nếu cuộc gọi API thất bại.
     */

    @Override
    public String extractText(byte[] imageBytes, String mimeType) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        log.info("Base64 Image Length: " + base64Image.length());

        // 1. Tạo JSON Request Body đúng định dạng cho Gemini API
        JSONObject payload = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        // Phần 1: Hướng dẫn mô hình (Prompt)
                                        .put(new JSONObject().put("text", "Extract all text from this image and return only the raw text."))
                                        // Phần 2: Dữ liệu hình ảnh (InlineData)
                                        .put(new JSONObject()
                                                .put("inlineData", new JSONObject()
                                                        .put("mimeType", mimeType)
                                                        .put("data", base64Image)
                                                )
                                        )
                                )
                        )
                );

        // Đảm bảo Content-Type là application/json
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(API_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            log.info("Gemini API Response: " + responseBody);

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                log.error("LỖI KHI GỌI GEMINI API - Status: {}, Body: {}", response.code(), responseBody);

                throw new IOException("Unexpected code " + response.code() + ". Error: " + errorBody);
            }

            // 2. Xử lý phản hồi (Response) đúng định dạng của Gemini API
            JSONObject result = new JSONObject(response.body().string());

            /*
             * Cấu trúc Response của Gemini API:
             * {
             * "candidates": [
             * {
             * "content": {
             * "parts": [
             * { "text": "VĂN BẢN TRÍCH XUẤT" }
             * ]
             * }
             * }
             * ]
             * }
             */

            JSONArray candidates = result.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0 && parts.getJSONObject(0).has("text")) {
                    return parts.getJSONObject(0).getString("text");
                }
            }

            return "No text or candidates found in the response.";

        }
    }

//    @Override
//    public String extractText(byte[] imageBytes, String mimeType) throws Exception {
//        return "";
//    }

    @Override
    public String getProviderName() {
        return "gemini";
    }
}