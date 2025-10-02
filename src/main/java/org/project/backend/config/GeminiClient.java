package org.project.backend.config;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Base64;

public class GeminiClient {
    private static final String API_KEY = "AIzaSyCV5ycD3IZEhemM5Aj9vbTnKS9tG3kGgls";
    private static final String API_URL =
            "https://vision.googleapis.com/v1/images:annotate" + "?key=" + API_KEY;

    /**
     * Trích xuất văn bản từ hình ảnh sử dụng Google Cloud Vision API.
     * @param imageBytes Mảng byte của hình ảnh đầu vào.
     * @return Văn bản đã trích xuất từ hình ảnh.
     * @throws IOException Nếu cuộc gọi API thất bại.
     */
    public static String extractTextFromImage(byte[] imageBytes) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 1. Tạo JSON Request Body đúng định dạng cho Cloud Vision API
        JSONObject payload = new JSONObject()
                .put("requests", new JSONArray()
                        .put(new JSONObject()
                                .put("image", new JSONObject()
                                        .put("content", base64Image)
                                )
                                // Chỉ định tính năng OCR (TEXT_DETECTION)
                                .put("features", new JSONArray()
                                        .put(new JSONObject()
                                                .put("type", "TEXT_DETECTION")
                                        )
                                )
                        )
                );

        // Đảm bảo Content-Type là application/json
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(API_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                // Ném lỗi chi tiết hơn
                throw new IOException("Unexpected code " + response.code() + ". Error: " + errorBody);
            }

            // 2. Xử lý phản hồi (Response) đúng định dạng của Cloud Vision API
            JSONObject result = new JSONObject(response.body().string());

            /*
             * Cấu trúc Response của Cloud Vision API:
             * {
             * "responses": [
             * {
             * "textAnnotations": [
             * { "description": "VĂN BẢN TRÍCH XUẤT HOÀN CHỈNH" },
             * ...
             * ]
             * }
             * ]
             * }
             */

            JSONArray responses = result.getJSONArray("responses");
            if (responses.length() > 0) {
                JSONObject firstResponse = responses.getJSONObject(0);
                if (firstResponse.has("textAnnotations")) {
                    JSONArray annotations = firstResponse.getJSONArray("textAnnotations");
                    if (annotations.length() > 0) {
                        // Trích xuất "description" từ phần tử đầu tiên (thường là toàn bộ văn bản)
                        return annotations.getJSONObject(0).getString("description");
                    }
                }
            }

            return "No text found in the image.";

        }
    }
}