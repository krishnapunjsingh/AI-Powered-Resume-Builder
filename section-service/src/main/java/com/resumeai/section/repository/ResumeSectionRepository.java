package com.resumeai.section.repository;

import com.resumeai.section.entity.ResumeSection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeSectionRepository extends JpaRepository<ResumeSection, Long> {

    List<ResumeSection> findByResumeIdOrderByDisplayOrderAsc(Long resumeId);
}
