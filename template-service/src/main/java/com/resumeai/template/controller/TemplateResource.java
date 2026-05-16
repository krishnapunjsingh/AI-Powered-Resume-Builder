package com.resumeai.template.controller;

import com.resumeai.template.dto.TemplateRequest;
import com.resumeai.template.dto.TemplateResponse;
import com.resumeai.template.service.TemplateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateResource {

    private final TemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateResponse create(@Valid @RequestBody TemplateRequest request) {
        return templateService.createTemplate(request);
    }

    @GetMapping("/{id}")
    public TemplateResponse getById(@PathVariable Integer id) {
        return templateService.getTemplateById(id);
    }

    @GetMapping
    public List<TemplateResponse> getAll() {
        return templateService.getAllTemplates();
    }

    @GetMapping("/free")
    public List<TemplateResponse> getFree() {
        return templateService.getFreeTemplates();
    }

    @GetMapping("/premium")
    public List<TemplateResponse> getPremium() {
        return templateService.getPremiumTemplates();
    }

    @GetMapping("/category/{category}")
    public List<TemplateResponse> getByCategory(@PathVariable String category) {
        return templateService.getByCategory(category);
    }

    @GetMapping("/popular")
    public List<TemplateResponse> getPopular() {
        return templateService.getPopularTemplates();
    }

    @PutMapping("/{id}")
    public TemplateResponse update(@PathVariable Integer id, @Valid @RequestBody TemplateRequest request) {
        return templateService.updateTemplate(id, request);
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Integer id) {
        templateService.deactivateTemplate(id);
    }

    @PutMapping("/{id}/usage/increment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void incrementUsage(@PathVariable Integer id) {
        templateService.incrementUsage(id);
    }

    @GetMapping("/admin/count")
    public long getTotalTemplatesCount() {
        return templateService.getTotalTemplatesCount();
    }
}