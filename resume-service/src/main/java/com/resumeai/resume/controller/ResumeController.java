package com.resumeai.resume.controller;

import com.resumeai.resume.dto.ResumeDetailsResponse;
import com.resumeai.resume.dto.ResumeRequest;
import com.resumeai.resume.dto.ResumeResponse;
import com.resumeai.resume.service.ResumeService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse create(@Valid @RequestBody ResumeRequest request) {
        return resumeService.create(request);
    }

    @PutMapping("/{id}")
    public ResumeResponse update(@PathVariable Long id, @Valid @RequestBody ResumeRequest request) {
        return resumeService.update(id, request);
    }

    @GetMapping("/{id}")
    public ResumeResponse getById(@PathVariable Long id) {
        return resumeService.getById(id);
    }

    @GetMapping("/{id}/details")
    public ResumeDetailsResponse getResumeWithSections(@PathVariable Long id,
                                                       @RequestHeader("Authorization") String authorizationHeader) {
        return resumeService.getResumeWithSections(id, authorizationHeader);
    }

    @GetMapping
    public List<ResumeResponse> getByUserId(@RequestParam Long userId) {
        return resumeService.getByUserId(userId);
    }

    @PostMapping("/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse duplicate(@PathVariable Long id) {
        return resumeService.duplicate(id);
    }

    @PutMapping("/{id}/publish")
    public ResumeResponse publish(@PathVariable Long id) {
        return resumeService.publish(id);
    }

    @PutMapping("/{id}/unpublish")
    public ResumeResponse unpublish(@PathVariable Long id) {
        return resumeService.unpublish(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        resumeService.delete(id);
    }

    @GetMapping("/admin/count")
    public long getTotalResumesCount() {
        return resumeService.getTotalResumesCount();
    }
}
