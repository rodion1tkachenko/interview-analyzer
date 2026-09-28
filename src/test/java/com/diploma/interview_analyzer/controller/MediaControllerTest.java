package com.diploma.interview_analyzer.controller;

import com.diploma.interview_analyzer.dto.MediaUploadResponse;
import com.diploma.interview_analyzer.service.MediaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MediaController.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Test
    @DisplayName("Успешная загрузка возвращает 201 Created и полные данные о файле в JSON")
    void uploadMedia_WhenValidFile_ShouldReturn201AndValidJsonResponse() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "interview.mp4",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "dummy video content".getBytes()
        );

        MediaUploadResponse mockResponse = new MediaUploadResponse(
                "test-file-id",
                "interview.mp4",
                1024L,
                "video/mp4",
                "UPLOADED",
                LocalDateTime.of(2026, 9, 28, 20, 12, 3)
        );

        when(mediaService.saveFile(any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(mockFile))
                .andExpect(status().isCreated())
                // Проверяем каждое поле JSON-ответа!
                .andExpect(jsonPath("$.fileId").value("test-file-id"))
                .andExpect(jsonPath("$.fileName").value("interview.mp4"))
                .andExpect(jsonPath("$.sizeBytes").value(1024))
                .andExpect(jsonPath("$.contentType").value("video/mp4"))
                .andExpect(jsonPath("$.status").value("UPLOADED"))
                .andExpect(jsonPath("$.uploadedAt").exists());

        verify(mediaService).saveFile(any());
    }

    @Test
    @DisplayName("Возвращает 400 Bad Request при отсутствии файла в запросе")
    void uploadMedia_WhenMissingFile_ShouldReturn400BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/v1/media/upload"))
                .andExpect(status().isBadRequest());
    }
}
