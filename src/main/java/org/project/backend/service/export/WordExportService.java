package org.project.backend.service.export;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class WordExportService implements ExportService {
    @Override
    public byte[] export(String text) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        doc.createParagraph().createRun().setText(text);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.write(bos);
        doc.close();
        return bos.toByteArray();
    }
}
