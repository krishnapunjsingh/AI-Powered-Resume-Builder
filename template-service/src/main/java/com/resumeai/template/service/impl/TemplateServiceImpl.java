package com.resumeai.template.service.impl;

import com.resumeai.template.dto.TemplateRequest;
import com.resumeai.template.dto.TemplateResponse;
import com.resumeai.template.entity.ResumeTemplate;
import com.resumeai.template.exception.TemplateNotFoundException;
import com.resumeai.template.repository.TemplateRepository;
import com.resumeai.template.service.TemplateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    @Override
    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        ResumeTemplate template = new ResumeTemplate();
        mapToEntity(template, request);
        template.setIsActive(request.isActive() == null ? true : request.isActive());
        template.setUsageCount(0);
        return mapToResponse(templateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Integer id) {
        ResumeTemplate template = templateRepository.findByTemplateIdAndIsActiveTrue(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found"));
        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findByIsActiveTrueOrderByUsageCountDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getFreeTemplates() {
        return templateRepository.findByIsPremiumAndIsActiveTrue(false)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getPremiumTemplates() {
        return templateRepository.findByIsPremiumAndIsActiveTrue(true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getByCategory(String category) {
        return templateRepository.findByCategoryIgnoreCaseAndIsActiveTrue(category)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public TemplateResponse updateTemplate(Integer id, TemplateRequest request) {
        ResumeTemplate template = templateRepository.findByTemplateId(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found"));

        mapToEntity(template, request);
        if (request.isActive() != null) {
            template.setIsActive(request.isActive());
        }
        return mapToResponse(templateRepository.save(template));
    }

    @Override
    @Transactional
    public void deactivateTemplate(Integer id) {
        ResumeTemplate template = templateRepository.findByTemplateId(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found"));
        template.setIsActive(false);
        templateRepository.save(template);
    }

    @Override
    @Transactional
    public void incrementUsage(Integer id) {
        int updated = templateRepository.incrementUsageCount(id);
        if (updated == 0) {
            throw new TemplateNotFoundException("Template not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getPopularTemplates() {
        return templateRepository.findTop10ByIsActiveTrueOrderByUsageCountDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void mapToEntity(ResumeTemplate template, TemplateRequest request) {
        template.setName(request.name());
        template.setDescription(request.description());
        template.setThumbnailUrl(request.thumbnailUrl());
        template.setHtmlLayout(request.htmlLayout());
        template.setCssStyles(request.cssStyles());
        template.setCategory(request.category());
        template.setIsPremium(request.isPremium());
    }

    private TemplateResponse mapToResponse(ResumeTemplate template) {
        return new TemplateResponse(
                template.getTemplateId(),
                template.getName(),
                template.getDescription(),
                template.getThumbnailUrl(),
                template.getHtmlLayout(),
                template.getCssStyles(),
                template.getCategory(),
                template.getIsPremium(),
                template.getIsActive(),
                template.getUsageCount(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    @Override
    public long getTotalTemplatesCount() {
        return templateRepository.countByIsActiveTrue();
    }
}