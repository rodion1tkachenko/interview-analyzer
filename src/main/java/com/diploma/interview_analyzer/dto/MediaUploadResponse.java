package com.diploma.interview_analyzer.dto;

import java.time.LocalDateTime;

public record MediaUploadResponse(
        String fileId,
        String fileName,
        long sizeBytes,
        String contentType,
        String status,
        LocalDateTime uploadedAt
) {}
