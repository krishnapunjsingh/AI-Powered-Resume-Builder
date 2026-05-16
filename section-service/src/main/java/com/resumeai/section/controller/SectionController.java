package com.resumeai.section.controller;

import com.resumeai.section.dto.SectionRequest;
import com.resumeai.section.dto.SectionResponse;
import com.resumeai.section.service.SectionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse create(@Valid @RequestBody SectionRequest request) {
        return sectionService.create(request);
    }

    @PutMapping("/{id}")
    public SectionResponse update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return sectionService.update(id, request);
    }

    @GetMapping
    public List<SectionResponse> getByResume(@RequestParam Long resumeId) {
        return sectionService.findByResumeId(resumeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sectionService.delete(id);
    }
}
