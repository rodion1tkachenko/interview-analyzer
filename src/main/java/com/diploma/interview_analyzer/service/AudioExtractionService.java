package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioExtractionService {

    private final MediaFileRepository mediaFileRepository;

    @Async
    public void extractAudioAsync(String mediaFileId) {
        // Шаг 1: В отдельной короткой транзакции меняем статус на PROCESSING_AUDIO
        MediaFileEntity mediaFile = updateStatus(mediaFileId, "PROCESSING_AUDIO");
        if (mediaFile == null) {
            log.error("Failed to find media file with id: {}", mediaFileId);
            return;
        }

        Path inputVideoPath = Paths.get(mediaFile.getFilePath());
        if (!Files.exists(inputVideoPath)) {
            log.error("Source video file does not exist on disk: {}", inputVideoPath);
            updateStatus(mediaFileId, "FAILED");
            return;
        }

        String outputAudioFilename = mediaFile.getId() + "_extracted.wav";
        Path outputAudioPath = inputVideoPath.getParent().resolve(outputAudioFilename);

        try {
            // Шаг 2: Вызов FFmpeg (выполняется в фоновом потоке ВНЕ ТРАНЗАКЦИИ БД)
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", inputVideoPath.toString(),
                    "-vn",
                    "-acodec", "pcm_s16le",
                    "-ar", "16000",
                    "-ac", "1",
                    outputAudioPath.toString()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg process failed with exit code: " + exitCode);
            }

            // Шаг 3: В новой короткой транзакции фиксируем успешное завершение
            updateStatus(mediaFileId, "AUDIO_EXTRACTED");
            log.info("Successfully extracted audio for mediaFileId {} to: {}", mediaFileId, outputAudioPath);

        } catch (Exception e) {
            log.error("Error during audio extraction for mediaFileId: {}", mediaFileId, e);
            updateStatus(mediaFileId, "FAILED");
        }
    }

    @Transactional
    public MediaFileEntity updateStatus(String mediaFileId, String status) {
        return mediaFileRepository.findById(mediaFileId)
                .map(entity -> {
                    entity.setStatus(status);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return mediaFileRepository.save(entity);
                })
                .orElse(null);
    }
}
