package org.project.backend.service.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component("excel")
public class ExcelExporter implements FileExporter {

    @Override
    public byte[] export(String content) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Tạo một sheet mới
            Sheet sheet = workbook.createSheet("OCR Result");

            // Tạo một hàng (row) ở vị trí 0
            Row headerRow = sheet.createRow(0);

            // Tạo một ô (cell) ở vị trí 0 và điền nội dung
            Cell cell = headerRow.createCell(0);
            cell.setCellValue(content);

            // Tự động điều chỉnh độ rộng cột cho vừa với nội dung
            sheet.autoSizeColumn(0);

            // Ghi workbook vào output stream
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public MediaType getMediaType() {
        // MediaType cho file .xlsx
        return MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Override
    public String getFileName() {
        return "ocr_result.xlsx";
    }

    @Override
    public String getFormatName() {
        return "excel";
    }
}