package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.dto.MediaUploadResponse;
import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final Path storageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Transactional
    public MediaUploadResponse saveFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot save empty file");
        }

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }

        String originalFilename = file.getOriginalFilename();
        String fileId = UUID.randomUUID().toString();
        String storedFileName = fileId + "_" + (originalFilename != null ? originalFilename : "media");

        Path targetLocation = this.storageLocation.resolve(storedFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }

        LocalDateTime now = LocalDateTime.now();

        MediaFileEntity entity = MediaFileEntity.builder()
                .id(fileId)
                .originalFileName(originalFilename)
                .storedFileName(storedFileName)
                .filePath(targetLocation.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .status("UPLOADED")
                .createdAt(now)
                .updatedAt(now)
                .build();

        mediaFileRepository.save(entity);

        return new MediaUploadResponse(
                fileId,
                originalFilename,
                file.getSize(),
                file.getContentType(),
                "UPLOADED",
                now
        );
    }
}
