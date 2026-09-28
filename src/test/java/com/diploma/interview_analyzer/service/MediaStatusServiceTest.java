package com.diploma.interview_analyzer.service;

import com.diploma.interview_analyzer.entity.MediaFileEntity;
import com.diploma.interview_analyzer.repository.MediaFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaStatusServiceTest {

    @Mock
    private MediaFileRepository mediaFileRepository;

    @InjectMocks
    private MediaStatusService mediaStatusService;

    @Test
    @DisplayName("Should update status and updatedAt timestamp when media file exists")
    void updateStatus_WhenFileExists_ShouldUpdateStatusAndTimestamp() {
        // Arrange
        String fileId = UUID.randomUUID().toString();
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);

        MediaFileEntity existingEntity = MediaFileEntity.builder()
                .id(fileId)
                .originalFileName("test.mp4")
                .status("UPLOADED")
                .createdAt(initialTime)
                .updatedAt(initialTime)
                .build();

        when(mediaFileRepository.findById(fileId)).thenReturn(Optional.of(existingEntity));
        when(mediaFileRepository.save(any(MediaFileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MediaFileEntity updatedEntity = mediaStatusService.updateStatus(fileId, "PROCESSING_AUDIO");

        // Assert
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getStatus()).isEqualTo("PROCESSING_AUDIO");
        assertThat(updatedEntity.getUpdatedAt()).isAfter(initialTime);

        verify(mediaFileRepository, times(1)).findById(fileId);
        verify(mediaFileRepository, times(1)).save(existingEntity);
    }

    @Test
    @DisplayName("Should return null and not save when media file does not exist")
    void updateStatus_WhenFileDoesNotExist_ShouldReturnNull() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(mediaFileRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        MediaFileEntity result = mediaStatusService.updateStatus(nonExistentId, "PROCESSING_AUDIO");

        // Assert
        assertThat(result).isNull();

        verify(mediaFileRepository, times(1)).findById(nonExistentId);
        verify(mediaFileRepository, never()).save(any());
    }
}
