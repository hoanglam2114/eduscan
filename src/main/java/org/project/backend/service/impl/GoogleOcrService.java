//package org.project.backend.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.project.backend.service.OcrService;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Base64;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class GoogleOcrService implements OcrService {
//
//    private final WebClient webClient;
//
////    @Value("${google.cloud.api-key}")
//    private String apiKey;
//
//    @Override
//    public OcrResult extractText(MultipartFile file) {
//        try {
////            String base64 = Base64Utils.encodeToString(file.getBytes());
//            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
//
//            Map<String, Object> body = Map.of(
//                    "requests", List.of(
//                            Map.of(
//                                    "image", Map.of("content", base64),
//                                    "features", List.of(Map.of("type", "DOCUMENT_TEXT_DETECTION"))
//                            )
//                    )
//            );
//
//            Map resp = webClient.post()
//                    .uri("https://vision.googleapis.com/v1/images:annotate?key=" + apiKey)
//                    .bodyValue(body)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            if (resp == null) {
//                return new OcrResult("No response from Google Vision", 0.0);
//            }
//
//            Map firstResp = (Map) ((List) resp.get("responses")).get(0);
//
//            String text = "";
//            if (firstResp.containsKey("fullTextAnnotation")) {
//                text = (String) ((Map) firstResp.get("fullTextAnnotation")).get("text");
//            }
//
//            return new OcrResult(text, 1.0);
//        } catch (Exception e) {
//            throw new RuntimeException("Google OCR failed", e);
//        }
//    }
//}
