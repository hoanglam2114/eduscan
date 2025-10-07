package org.project.backend.service.export;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component("pdf")
public class PdfExporter implements FileExporter {

    @Override
    public byte[] export(String content) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Chọn font chữ hỗ trợ Unicode để hiển thị tiếng Việt
            // StandardFonts.HELVETICA có thể không đủ, bạn nên nhúng font .ttf vào project nếu cần
            // Ví dụ: PdfFont font = PdfFontFactory.createFont("src/main/resources/fonts/NotoSans-Regular.ttf", true);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            document.setFont(font);

            // Thêm nội dung vào file PDF
            Paragraph para = new Paragraph(content)
                    .setTextAlignment(TextAlignment.LEFT);
            document.add(para);

            // Đóng document để hoàn tất việc ghi file
            document.close();

            return out.toByteArray();
        }
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_PDF;
    }

    @Override
    public String getFileName() {
        return "ocr_result.pdf";
    }

    @Override
    public String getFormatName() {
        return "pdf";
    }
}