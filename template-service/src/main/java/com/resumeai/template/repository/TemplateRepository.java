package com.resumeai.template.repository;

import com.resumeai.template.entity.ResumeTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplateRepository extends JpaRepository<ResumeTemplate, Integer> {

    Optional<ResumeTemplate> findByTemplateId(Integer templateId);

    Optional<ResumeTemplate> findByTemplateIdAndIsActiveTrue(Integer templateId);

    List<ResumeTemplate> findByCategoryIgnoreCaseAndIsActiveTrue(String category);

    List<ResumeTemplate> findByIsPremiumAndIsActiveTrue(Boolean isPremium);

    List<ResumeTemplate> findByIsActiveTrueOrderByUsageCountDesc();

    List<ResumeTemplate> findTop10ByIsActiveTrueOrderByUsageCountDesc();

    long countByCategoryIgnoreCase(String category);

    long countByIsActiveTrue();

    @Modifying
    @Query("update ResumeTemplate t set t.usageCount = t.usageCount + 1 where t.templateId = :templateId and t.isActive = true")
    int incrementUsageCount(@Param("templateId") Integer templateId);
}