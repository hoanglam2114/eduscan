package org.project.backend.service.assist;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component("geminiLlm")
public class GeminiLlmProvider implements LlmProvider {

    @Value("${api.google.gemini.key}")
    private String apiKey;

    private static final String MODEL_NAME = "gemini-2.5-flash-lite"; // Hoặc "gemini-1.5-flash" nếu bạn muốn dùng model đó
    private static final String API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final OkHttpClient client;

    public GeminiLlmProvider() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String generateContent(String prompt) throws Exception {
//        String url = String.format(API_URL_TEMPLATE, apiKey);
        String url = String.format(API_URL_TEMPLATE, MODEL_NAME, apiKey);


        // Tạo payload JSON cho Gemini API
        JSONObject payload = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt))
                                )
                        )
                )
                .put("generationConfig", new JSONObject()
                        .put("temperature", 0.7)
                        .put("topK", 40)
                        .put("topP", 0.95)
                        .put("maxOutputTokens", 2048)
                );

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("Gemini API request failed: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            return extractTextFromResponse(responseBody);
        }
    }

    /**
     * Trích xuất text từ response của Gemini API
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);

            if (!jsonResponse.has("candidates")) {
                throw new RuntimeException("No candidates in response");
            }

            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() == 0) {
                throw new RuntimeException("Empty candidates array");
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("text")) {
                    result.append(part.getString("text"));
                }
            }

            return result.toString().trim();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "geminiLlm";
    }
}