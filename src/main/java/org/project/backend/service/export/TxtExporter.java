package org.project.backend.service.export;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component("txt") // Đặt tên cho bean là "txt"
public class TxtExporter implements FileExporter {

    @Override
    public byte[] export(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.TEXT_PLAIN;
    }

    @Override
    public String getFileName() {
        return "result.txt";
    }

    @Override
    public String getFormatName() {
        return "txt";
    }
}