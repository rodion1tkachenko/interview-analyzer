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
    private final MediaStatusService mediaStatusService;
    private final TranscriptionService transcriptionService;

    @Async
    public void extractAudioAsync(String mediaFileId) {
        MediaFileEntity mediaFile = mediaStatusService.updateStatus(mediaFileId, "PROCESSING_AUDIO");
        if (mediaFile == null) {
            return;
        }

        Path inputVideoPath = Paths.get(mediaFile.getFilePath());
        if (!Files.exists(inputVideoPath)) {
            log.error("Source video file does not exist on disk: {}", inputVideoPath);
            mediaStatusService.markFailed(mediaFileId, "Source file not found: " + inputVideoPath);
            return;
        }

        String outputAudioFilename = mediaFile.getId() + "_extracted.wav";
        Path outputAudioPath = inputVideoPath.getParent().resolve(outputAudioFilename);

        try {
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

            mediaStatusService.markAudioExtracted(mediaFileId, outputAudioPath.toString());
            log.info("Successfully extracted audio for mediaFileId {} to: {}", mediaFileId, outputAudioPath);

            transcriptionService.transcribeAsync(mediaFileId);

        } catch (Exception e) {
            log.error("Error during audio extraction for mediaFileId: {}", mediaFileId, e);
            mediaStatusService.markFailed(mediaFileId, e.getMessage());
        }
    }
}