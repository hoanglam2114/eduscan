package org.project.backend.service.ocr;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Base64;

@Component("vision") // Đặt tên cho bean này là "vision"
public class GoogleVisionOcrProvider implements OcrProvider {

    @Value("${api.google.vision.key}") // Lấy API key từ application.properties
    private String apiKey;

    private static final String API_URL_TEMPLATE = "https://vision.googleapis.com/v1/images:annotate?key=%s";

    @Override
    public String extractText(byte[] imageBytes, String mimeType) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        JSONObject payload = new JSONObject()
                .put("requests", new JSONArray()
                        .put(new JSONObject()
                                .put("image", new JSONObject().put("content", base64Image))
                                .put("features", new JSONArray()
                                        .put(new JSONObject().put("type", "TEXT_DETECTION"))
                                )
                        )
                );

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(String.format(API_URL_TEMPLATE, apiKey)).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response.code() + ". Error: " + response.body().string());
            }

            JSONObject result = new JSONObject(response.body().string());
            JSONArray responses = result.getJSONArray("responses");
            if (responses.length() > 0) {
                JSONObject firstResponse = responses.getJSONObject(0);
                if (firstResponse.has("textAnnotations")) {
                    JSONArray annotations = firstResponse.getJSONArray("textAnnotations");
                    if (annotations.length() > 0) {
                        return annotations.getJSONObject(0).getString("description");
                    }
                }
            }
            return "No text found in the image.";
        }
    }

    @Override
    public String getProviderName() {
        return "vision";
    }
}