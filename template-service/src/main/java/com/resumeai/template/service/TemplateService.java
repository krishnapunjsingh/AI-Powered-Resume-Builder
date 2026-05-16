package com.resumeai.template.service;

import com.resumeai.template.dto.TemplateRequest;
import com.resumeai.template.dto.TemplateResponse;
import java.util.List;

public interface TemplateService {

    TemplateResponse createTemplate(TemplateRequest request);

    TemplateResponse getTemplateById(Integer id);

    List<TemplateResponse> getAllTemplates();

    List<TemplateResponse> getFreeTemplates();

    List<TemplateResponse> getPremiumTemplates();

    List<TemplateResponse> getByCategory(String category);

    TemplateResponse updateTemplate(Integer id, TemplateRequest request);

    void deactivateTemplate(Integer id);

    void incrementUsage(Integer id);

    List<TemplateResponse> getPopularTemplates();

    long getTotalTemplatesCount();
}