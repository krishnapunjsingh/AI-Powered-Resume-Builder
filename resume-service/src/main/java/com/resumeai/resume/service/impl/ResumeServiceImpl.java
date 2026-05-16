package com.resumeai.resume.service.impl;

import com.resumeai.resume.client.AuthServiceClient;
import com.resumeai.resume.client.NotificationServiceClient;
import com.resumeai.resume.client.SectionServiceClient;
import com.resumeai.resume.dto.ProfileResponse;
import com.resumeai.resume.dto.ResumeDetailsResponse;
import com.resumeai.resume.dto.ResumeRequest;
import com.resumeai.resume.dto.ResumeResponse;
import com.resumeai.resume.entity.Resume;
import com.resumeai.resume.exception.InvalidResumeStatusException;
import com.resumeai.resume.exception.ResumeNotFoundException;
import com.resumeai.resume.exception.UserNotFoundException;
import com.resumeai.resume.repository.ResumeRepository;
import com.resumeai.resume.service.ResumeService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private static final Set<String> ALLOWED_STATUS = Set.of("DRAFT", "COMPLETE");

    private final ResumeRepository resumeRepository;
    private final SectionServiceClient sectionServiceClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public ResumeResponse create(ResumeRequest request) {
        validateUserExists(request.userId());
        Resume resume = resumeRepository.save(mapToEntity(request, Resume.builder().build()));
        sendResumeSavedNotification(resume, true);
        return mapToResponse(resume);
    }

    @Override
    @Transactional
    public ResumeResponse update(Long id, ResumeRequest request) {
        validateUserExists(request.userId());
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException(id));

        resume = resumeRepository.save(mapToEntity(request, resume));
        sendResumeSavedNotification(resume, false);
        return mapToResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getById(Long id) {
        return resumeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResumeNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDetailsResponse getResumeWithSections(Long id, String authorizationHeader) {
        ResumeResponse resume = getById(id);
        return new ResumeDetailsResponse(
                resume,
                sectionServiceClient.getSectionsByResumeId(id, authorizationHeader)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getByUserId(Long userId) {
        validateUserExists(userId);
        return resumeRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

        @Override
        @Transactional
        public ResumeResponse duplicate(Long id) {
        Resume source = resumeRepository.findById(id)
            .orElseThrow(() -> new ResumeNotFoundException(id));

        Resume duplicate = Resume.builder()
            .userId(source.getUserId())
            .title("Copy of " + source.getTitle())
            .summary(source.getSummary())
            .status("DRAFT")
            .isPublic(false)
            .build();

        return mapToResponse(resumeRepository.save(duplicate));
        }

        @Override
        @Transactional
        public ResumeResponse publish(Long id) {
        Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new ResumeNotFoundException(id));
        resume.setPublic(true);
        return mapToResponse(resumeRepository.save(resume));
        }

        @Override
        @Transactional
        public ResumeResponse unpublish(Long id) {
        Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new ResumeNotFoundException(id));
        resume.setPublic(false);
        return mapToResponse(resumeRepository.save(resume));
        }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!resumeRepository.existsById(id)) {
            throw new ResumeNotFoundException(id);
        }
        resumeRepository.deleteById(id);
    }

    private Resume mapToEntity(ResumeRequest request, Resume resume) {
        resume.setUserId(request.userId());
        resume.setTitle(request.title());
        resume.setSummary(request.summary());
        resume.setStatus(normalizeStatus(request.status()));
        return resume;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUS.contains(normalized)) {
            throw new InvalidResumeStatusException(status);
        }
        return normalized;
    }

    private void validateUserExists(Long userId) {
        if (!authServiceClient.userExists(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private void sendResumeSavedNotification(Resume resume, boolean created) {
        try {
            ProfileResponse profile = authServiceClient.getProfile(resume.getUserId());
            String action = created ? "created" : "updated";
            String title = created ? "Resume created" : "Resume saved";
            String message = "Your resume \"" + resume.getTitle() + "\" was " + action + " successfully.";
            notificationServiceClient.sendToAppAndEmail(
                    resume.getUserId(),
                    profile.email(),
                    created ? "RESUME_CREATED" : "RESUME_UPDATED",
                    title,
                    message,
                    resume.getId()
            );
        } catch (Exception exception) {
            log.warn("Could not send resume saved notification for resume {}", resume.getId(), exception);
        }
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getUserId(),
                resume.getTitle(),
                resume.getSummary(),
                resume.getStatus(),
                resume.isPublic(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }

    @Override
    public long getTotalResumesCount() {
        return resumeRepository.count();
    }
}
