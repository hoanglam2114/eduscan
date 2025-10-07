package org.project.backend.service;

import org.project.backend.model.ExportResult;
import org.project.backend.service.export.FileExporter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final Map<String, FileExporter> exporters;

    public ExportService(List<FileExporter> exporterList) {
        this.exporters = exporterList.stream()
                .collect(Collectors.toMap(FileExporter::getFormatName, Function.identity()));
    }

    public ExportResult export(String content, String format) throws Exception {
        FileExporter exporter = exporters.get(format.toLowerCase());
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        byte[] data = exporter.export(content);
        return new ExportResult(data, exporter.getMediaType(), exporter.getFileName());
    }

    public List<String> getAvailableFormats() {
        return exporters.keySet().stream().collect(Collectors.toList());
    }
}