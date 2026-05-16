package com.resumeai.resume.client;

import com.resumeai.resume.dto.SectionSummary;
import com.resumeai.resume.feign.SectionServiceFeignClient;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SectionServiceClient {

    private final SectionServiceFeignClient sectionServiceFeignClient;

    public SectionServiceClient(SectionServiceFeignClient sectionServiceFeignClient) {
        this.sectionServiceFeignClient = sectionServiceFeignClient;
    }

    public List<SectionSummary> getSectionsByResumeId(Long resumeId, String authorizationHeader) {
        try {
            return sectionServiceFeignClient.getSectionsByResumeId(resumeId, authorizationHeader);
        } catch (Exception exception) {
            return List.of();
        }
    }
}
