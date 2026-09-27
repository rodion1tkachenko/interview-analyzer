package com.diploma.interview_analyzer.controller;

import com.diploma.interview_analyzer.dto.MediaUploadResponse;
import com.diploma.interview_analyzer.service.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadMedia(@RequestParam("file") MultipartFile file) {
        MediaUploadResponse response = mediaService.saveFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
