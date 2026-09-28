package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStatusService {

    private final MediaFileRepository mediaFileRepository;

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
}
