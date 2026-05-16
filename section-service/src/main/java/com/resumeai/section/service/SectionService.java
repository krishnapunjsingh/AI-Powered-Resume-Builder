package com.resumeai.section.service;

import com.resumeai.section.dto.SectionRequest;
import com.resumeai.section.dto.SectionResponse;
import java.util.List;

public interface SectionService {

    SectionResponse create(SectionRequest request);

    SectionResponse update(Long id, SectionRequest request);

    List<SectionResponse> findByResumeId(Long resumeId);

    void delete(Long id);
}
