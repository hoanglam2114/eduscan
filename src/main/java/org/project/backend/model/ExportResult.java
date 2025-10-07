package org.project.backend.model;

import org.springframework.http.MediaType;

public class ExportResult {
    private final byte[] data;
    private final MediaType mediaType;
    private final String fileName;

    // Constructor, getters...
    public ExportResult(byte[] data, MediaType mediaType, String fileName) {
        this.data = data;
        this.mediaType = mediaType;
        this.fileName = fileName;
    }

    public byte[] getData() { return data; }
    public MediaType getMediaType() { return mediaType; }
    public String getFileName() { return fileName; }
}