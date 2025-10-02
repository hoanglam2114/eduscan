package org.project.backend.service.export;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class PdfExportService implements ExportService {
    @Override
    public byte[] export(String text) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, bos);
        doc.open();
        doc.add(new Paragraph(text));
        doc.close();
        return bos.toByteArray();
    }
}
