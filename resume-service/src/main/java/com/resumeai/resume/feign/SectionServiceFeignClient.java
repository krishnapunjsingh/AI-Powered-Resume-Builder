package com.resumeai.resume.feign;

import com.resumeai.resume.dto.SectionSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "section-service", url = "http://localhost:8083")
public interface SectionServiceFeignClient {

    @GetMapping("/sections")
    List<SectionSummary> getSectionsByResumeId(
            @RequestParam("resumeId") Long resumeId,
            @RequestHeader("Authorization") String authorizationHeader
    );
}
