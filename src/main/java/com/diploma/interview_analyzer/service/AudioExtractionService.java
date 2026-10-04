package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioExtractionService {

    private final MediaFileRepository mediaFileRepository;
    private final MediaStatusService mediaStatusService; // Внедряем Helper

    @Async
    public void extractAudioAsync(String mediaFileId) {
        // 1. Вызов ДРУГОГО бина через Spring Proxy -> Честная новая транзакция!
        MediaFileEntity mediaFile = mediaStatusService.updateStatus(mediaFileId, "PROCESSING_AUDIO");
        if (mediaFile == null) {
            return;
        }

        Path inputVideoPath = Paths.get(mediaFile.getFilePath());
        if (!Files.exists(inputVideoPath)) {
            log.error("Source video file does not exist on disk: {}", inputVideoPath);
            mediaStatusService.updateStatus(mediaFileId, "FAILED");
            return;
        }

        String outputAudioFilename = mediaFile.getId() + "_extracted.wav";
        Path outputAudioPath = inputVideoPath.getParent().resolve(outputAudioFilename);

        try {
            // 2. Процесс FFmpeg (выполняется полностью ВНЕ транзакций)
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

            // 3. Фиксируем успех в еще одной короткой транзакции
            mediaStatusService.updateStatus(mediaFileId, "AUDIO_EXTRACTED");
            log.info("Successfully extracted audio for mediaFileId {} to: {}", mediaFileId, outputAudioPath);

        } catch (Exception e) {
            log.error("Error during audio extraction for mediaFileId: {}", mediaFileId, e);
            mediaStatusService.updateStatus(mediaFileId, "FAILED");
        }
    }
}
