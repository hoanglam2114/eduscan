package fpt.eduscan.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiOCRService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiOCRService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extract text from image using Gemini Vision API
     */
    public String extractTextFromImage(MultipartFile file) throws Exception {
        // Convert image to base64
        byte[] imageBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Get mime type
        String mimeType = file.getContentType();
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }

        // Build request body for Gemini API
        Map<String, Object> requestBody = buildGeminiRequest(base64Image, mimeType);

        // Build the correct API URL with key
        String fullUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;

        System.out.println("Calling Gemini API...");

        // Call Gemini API
        String response = webClient.post()
                .uri(fullUrl)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Gemini API Response received");

        // Parse response and extract text
        return parseGeminiResponse(response);
    }

    /**
     * Build request body for Gemini API
     */
    private Map<String, Object> buildGeminiRequest(String base64Image, String mimeType) {
        Map<String, Object> request = new HashMap<>();

        // Parts for the content
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", "\"Extract only the text from this image. Do not explain or give steps. If there is no text, say 'No text found'.\\n\\nImage (base64): <base64_image_here>\"");

        Map<String, Object> imagePart = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);
        imagePart.put("inline_data", inlineData);

        // Contents array
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart, imagePart));

        request.put("contents", List.of(content));

        return request;
    }

    /**
     * Parse Gemini API response and extract text
     */
    private String parseGeminiResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);

        // Check for error in response
        if (root.has("error")) {
            JsonNode error = root.get("error");
            String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";
            throw new Exception("Gemini API Error: " + errorMessage);
        }

        // Navigate through JSON structure
        JsonNode candidates = root.get("candidates");
        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.get("content");
            if (content != null) {
                JsonNode parts = content.get("parts");
                if (parts != null && parts.isArray() && parts.size() > 0) {
                    JsonNode text = parts.get(0).get("text");
                    if (text != null) {
                        return text.asText();
                    }
                }
            }
        }
        throw new Exception("Could not extract text from Gemini response. Response: " + jsonResponse);
    }
}