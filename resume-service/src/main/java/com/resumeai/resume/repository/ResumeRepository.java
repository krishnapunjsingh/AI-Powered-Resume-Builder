package com.resumeai.resume.repository;

import com.resumeai.resume.entity.Resume;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserId(Long userId);
    
    long count();
}
