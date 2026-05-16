package com.resumeai.resume.service;

import com.resumeai.resume.dto.ResumeDetailsResponse;
import com.resumeai.resume.dto.ResumeRequest;
import com.resumeai.resume.dto.ResumeResponse;
import java.util.List;

public interface ResumeService {

    ResumeResponse create(ResumeRequest request);

    ResumeResponse update(Long id, ResumeRequest request);

    ResumeResponse getById(Long id);

    ResumeDetailsResponse getResumeWithSections(Long id, String authorizationHeader);

    List<ResumeResponse> getByUserId(Long userId);

    ResumeResponse duplicate(Long id);

    ResumeResponse publish(Long id);

    ResumeResponse unpublish(Long id);

    void delete(Long id);

    long getTotalResumesCount();
}
