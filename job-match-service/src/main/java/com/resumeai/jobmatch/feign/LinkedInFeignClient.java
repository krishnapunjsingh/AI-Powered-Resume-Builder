package com.resumeai.jobmatch.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "linkedin", url = "${jobmatch.external.linkedin.base-url:}")
public interface LinkedInFeignClient {

    @GetMapping
    JsonNode fetchJobs(
            @RequestParam("keywords") String keywords,
            @RequestParam("location") String location
    );
}
