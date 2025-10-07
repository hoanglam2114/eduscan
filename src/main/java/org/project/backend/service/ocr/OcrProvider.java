package org.project.backend.service.ocr;


/**
 * Giao diện cho các nhà cung cấp OCR (Optical Character Recognition).
 * Các triển khai cụ thể sẽ sử dụng các dịch vụ OCR khác nhau như Google Gemini, Google Vision, v.v.
 */
public interface OcrProvider {
    /**
     * Trích xuất văn bản từ một mảng byte của hình ảnh.
     * @param imageBytes Dữ liệu hình ảnh.
     * @param mimeType Kiểu MIME của hình ảnh (ví dụ: "image/jpeg").
     * @return Văn bản đã trích xuất.
     */
    String extractText(byte[] imageBytes, String mimeType) throws Exception;

    /**
     * Lấy tên của nhà cung cấp để định danh.
     * @return Tên nhà cung cấp (ví dụ: "gemini", "vision").
     */
    String getProviderName();
}