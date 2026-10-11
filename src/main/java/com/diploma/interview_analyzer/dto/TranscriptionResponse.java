package com.diploma.interview_analyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TranscriptionResponse(String language, double duration,
                                    String text, List<Segment> segments) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Segment(double start, double end, String text) {}
}