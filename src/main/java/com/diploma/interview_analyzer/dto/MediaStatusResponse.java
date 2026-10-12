package com.diploma.interview_analyzer.dto;

public record MediaStatusResponse(String id,
                                  String status,
                                  String transcript,
                                  String errorMessage) {
}