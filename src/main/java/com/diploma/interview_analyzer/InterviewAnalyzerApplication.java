package com.diploma.interview_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class InterviewAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewAnalyzerApplication.class, args);
    }

}
