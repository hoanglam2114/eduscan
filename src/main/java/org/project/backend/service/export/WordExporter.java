package org.project.backend.service.export;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component("word")
public class WordExporter implements FileExporter {

    @Override
    public byte[] export(String content) throws Exception {
        // Sử dụng try-with-resources để đảm bảo document được đóng đúng cách
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Tạo một đoạn văn bản (paragraph)
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(content);

            // Ghi document vào một ByteArrayOutputStream
            document.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public MediaType getMediaType() {
        // MediaType cho file .docx
        return MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Override
    public String getFileName() {
        return "ocr_result.docx";
    }

    @Override
    public String getFormatName() {
        return "word";
    }
}