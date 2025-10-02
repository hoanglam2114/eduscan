package org.project.backend.service.export;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class ExcelExportService implements ExportService {
    @Override
    public byte[] export(String text) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("OCR Result");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(text);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return bos.toByteArray();
    }
}
