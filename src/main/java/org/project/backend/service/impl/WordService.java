package org.project.backend.service.impl;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.project.backend.service.DocumentService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@Service
public class WordService implements DocumentService {
    @Override
    public byte[] createDocx(String text) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Arrays.stream(text.split("\\r?\\n")).forEach(line -> {
                XWPFParagraph p = doc.createParagraph();
                p.createRun().setText(line);
            });

            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error creating DOCX", e);
        }
    }
}
