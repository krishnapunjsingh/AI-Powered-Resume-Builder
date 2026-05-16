package com.resumeai.section.service.impl;

import com.resumeai.section.dto.SectionRequest;
import com.resumeai.section.dto.SectionResponse;
import com.resumeai.section.entity.ResumeSection;
import com.resumeai.section.repository.ResumeSectionRepository;
import com.resumeai.section.service.SectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final ResumeSectionRepository resumeSectionRepository;

    @Override
    @Transactional
    public SectionResponse create(SectionRequest request) {
        ResumeSection section = resumeSectionRepository.save(mapToEntity(request, ResumeSection.builder().build()));
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse update(Long id, SectionRequest request) {
        ResumeSection section = resumeSectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));

        section = resumeSectionRepository.save(mapToEntity(request, section));
        return mapToResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> findByResumeId(Long resumeId) {
        return resumeSectionRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        resumeSectionRepository.deleteById(id);
    }

    private ResumeSection mapToEntity(SectionRequest request, ResumeSection section) {
        section.setResumeId(request.resumeId());
        section.setSectionName(request.sectionName());
        section.setContent(request.content());
        section.setDisplayOrder(request.displayOrder());
        return section;
    }

    private SectionResponse mapToResponse(ResumeSection section) {
        return new SectionResponse(
                section.getId(),
                section.getResumeId(),
                section.getSectionName(),
                section.getContent(),
                section.getDisplayOrder(),
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }
}
