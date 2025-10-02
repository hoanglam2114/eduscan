package org.project.backend.config;


import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Base64;

public class GeminiClient {
    private static final String API_KEY = "AIzaSyCZeiFnsPJRy6wmZTi6OXzkSQnMRKKG88E";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent" +"?key="+ API_KEY;

    public static String extractTextFromImage(byte[] imageBytes) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        JSONObject payload = new JSONObject()
                .put("contents", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("parts", new org.json.JSONArray()
                                        .put(new JSONObject().put("text", "Extract text from this image"))
                                        .put(new JSONObject()
                                                .put("inlineData", new JSONObject()
                                                        .put("mimeType", "image/png")
                                                        .put("data", base64Image)
                                                )
                                        )
                                )
                        )
                );

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(API_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            JSONObject result = new JSONObject(response.body().string());
            return result
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        }
    }
}
