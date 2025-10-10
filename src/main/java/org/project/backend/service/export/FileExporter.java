package org.project.backend.service.export;

import org.springframework.http.MediaType;

public interface FileExporter {
    /**
     * Chuyển đổi nội dung văn bản thành mảng byte của file.
     * @param content Nội dung văn bản.
     * @return Mảng byte của file.
     */
    byte[] export(String content) throws Exception;

    /**
     * Lấy kiểu Media Type cho HTTP response.
     * @return MediaType.
     */
    MediaType getMediaType();

    /**
     * Lấy tên file mặc định.
     * @return Tên file (ví dụ: "result.docx").
     */
    String getFileName();

    /**
     * Lấy tên định dạng để định danh.
     * @return Tên định dạng (ví dụ: "word", "pdf").
     */
    String getFormatName();
}