package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.dto.TranscriptionResponse;
import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStatusService {

    private final MediaFileRepository mediaFileRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MediaFileEntity updateStatus(String mediaFileId, String status) {
        log.info("Updating status for mediaFileId {} to {}", mediaFileId, status);

        return mediaFileRepository.findById(mediaFileId)
                .map(entity -> {
                    entity.setStatus(status);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return mediaFileRepository.save(entity);
                })
                .orElseGet(() -> {
                    log.error("Failed to update status. File not found: {}", mediaFileId);
                    return null;
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAudioExtracted(String mediaFileId, String audioPath) {
        mediaFileRepository.findById(mediaFileId).ifPresent(entity -> {
            entity.setAudioPath(audioPath);
            entity.setStatus("AUDIO_EXTRACTED");
            entity.setUpdatedAt(LocalDateTime.now());
            mediaFileRepository.save(entity);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTranscript(String mediaFileId, TranscriptionResponse response) {
        mediaFileRepository.findById(mediaFileId).ifPresent(entity -> {
            try {
                entity.setTranscript(response.text());
                entity.setTranscriptSegments(objectMapper.writeValueAsString(response.segments()));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot serialize transcript segments", e);
            }
            entity.setStatus("TRANSCRIBED");
            entity.setUpdatedAt(LocalDateTime.now());
            mediaFileRepository.save(entity);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String mediaFileId, String errorMessage) {
        mediaFileRepository.findById(mediaFileId).ifPresent(entity -> {
            entity.setStatus("FAILED");
            entity.setErrorMessage(errorMessage);
            entity.setUpdatedAt(LocalDateTime.now());
            mediaFileRepository.save(entity);
        });
    }
}