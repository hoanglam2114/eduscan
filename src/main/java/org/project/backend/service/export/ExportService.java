package org.project.backend.service.export;


public interface ExportService {
    byte[] export(String text) throws Exception;
}
