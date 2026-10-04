package com.diploma.interview_analyzer.repository;

import com.diploma.interview_analyzer.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFileEntity, String> {
}
