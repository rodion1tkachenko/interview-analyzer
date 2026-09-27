package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public Path extractAudio(String mediaFileId) {
        MediaFileEntity mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found: " + mediaFileId));

        Path inputVideoPath = Paths.get(mediaFile.getFilePath());
        if (!Files.exists(inputVideoPath)) {
            mediaFile.setStatus("FAILED");
            mediaFile.setUpdatedAt(LocalDateTime.now());
            mediaFileRepository.save(mediaFile);
            throw new IllegalStateException("Source video file does not exist on disk: " + inputVideoPath);
        }

        // Формируем путь для сгенерированного .wav файла
        String outputAudioFilename = mediaFile.getId() + "_extracted.wav";
        Path outputAudioPath = inputVideoPath.getParent().resolve(outputAudioFilename);

        // Меняем статус в БД на PROCESSING_AUDIO
        mediaFile.setStatus("PROCESSING_AUDIO");
        mediaFile.setUpdatedAt(LocalDateTime.now());
        mediaFileRepository.save(mediaFile);

        try {
            // Команда FFmpeg:
            // -i: входной файл
            // -vn: отключить видеопоток
            // -acodec pcm_s16le: 16-битный PCM кодек для WAV
            // -ar 16000: частота 16 kHz (оптимально для Speech-to-Text / Whisper)
            // -ac 1: моно-звук
            // -y: перезаписать выходной файл, если он уже существует
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

            // Читаем вывод FFmpeg для отладки в логах
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

            // Обновляем статус и сохраняем успешный результат в БД
            mediaFile.setStatus("AUDIO_EXTRACTED");
            mediaFile.setUpdatedAt(LocalDateTime.now());
            mediaFileRepository.save(mediaFile);

            log.info("Successfully extracted audio to: {}", outputAudioPath);
            return outputAudioPath;

        } catch (Exception e) {
            log.error("Error during audio extraction for mediaFileId: {}", mediaFileId, e);
            mediaFile.setStatus("FAILED");
            mediaFile.setUpdatedAt(LocalDateTime.now());
            mediaFileRepository.save(mediaFile);
            throw new RuntimeException("Failed to extract audio", e);
        }
    }
}
