package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.client.SttClient;
import com.diploma.interview_analyzer.dto.TranscriptionResponse;
import com.diploma.interview_analyzer.entity.MediaFileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptionService {

    private final MediaStatusService mediaStatusService;
    private final SttClient sttClient;

    @Async("sttExecutor")
    public void transcribeAsync(String mediaFileId) {
        MediaFileEntity mediaFile = mediaStatusService.updateStatus(mediaFileId, "TRANSCRIBING");
        if (mediaFile == null) {
            return;
        }

        try {
            TranscriptionResponse response = sttClient.transcribe(Path.of(mediaFile.getAudioPath()));
            mediaStatusService.saveTranscript(mediaFileId, response);
            log.info("Transcription finished for mediaFileId {}", mediaFileId);
        } catch (Exception e) {
            log.error("Transcription failed for mediaFileId {}", mediaFileId, e);
            mediaStatusService.markFailed(mediaFileId, e.getMessage());
        }
    }
}