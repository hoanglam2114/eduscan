package org.project.backend.service.ocr;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component("ocrspace") // Đăng ký bean này với tên là "ocrspace"
public class OcrSpaceProvider implements OcrProvider {

    private static final Logger log = LoggerFactory.getLogger(OcrSpaceProvider.class);


    @Value("${api.ocrspace.key}")
    private String apiKey;

    private static final String API_URL = "https://api.ocr.space/parse/image";

    @Override
    public String extractText(byte[] imageBytes, String mimeType) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 1. Tạo request body dạng multipart/form-data
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apikey", apiKey) // Thêm API key
                .addFormDataPart("language", "eng") // Chỉ định ngôn ngữ là tiếng Việt
//                .addFormDataPart("ocrengine", "2") // Sử dụng OCR Engine 2 (nâng cao hơn)
                .addFormDataPart("isOverlayRequired", "false")
                // Thêm file ảnh vào request
                .addFormDataPart("file", "image.jpg", RequestBody.create(imageBytes, MediaType.parse(mimeType)))
                .build();

        // 2. Tạo request POST
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        // 3. Gửi request và xử lý response
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            if (!response.isSuccessful()) {
                log.error("Lỗi gọi OCR.space API - Status: {}, Body: {}", response.code(), responseBody);
                throw new IOException("Unexpected code " + response.code() + ". Error: " + responseBody);
            }

            JSONObject result = new JSONObject(responseBody);

            if (result.getBoolean("IsErroredOnProcessing")) {
                // SỬA LỖI TẠI ĐÂY
                String errorMessage;
                Object errorObj = result.get("ErrorMessage");

                if (errorObj instanceof JSONArray) {
                    // Nếu là mảng, nối các phần tử lại thành một chuỗi duy nhất
                    JSONArray errorArray = (JSONArray) errorObj;
                    errorMessage = IntStream.range(0, errorArray.length())
                            .mapToObj(errorArray::getString)
                            .collect(Collectors.joining(", "));
                } else {
                    // Nếu là chuỗi, lấy giá trị như bình thường
                    errorMessage = errorObj.toString();
                }

                throw new IOException("OCR.space API Error: " + errorMessage);
            }

            JSONArray parsedResults = result.getJSONArray("ParsedResults");
            if (parsedResults.length() > 0) {
                return parsedResults.getJSONObject(0).getString("ParsedText");
            }

            return "No text found by OCR.space.";
        }
    }

    @Override
    public String getProviderName() {
        return "ocrspace";
    }
}