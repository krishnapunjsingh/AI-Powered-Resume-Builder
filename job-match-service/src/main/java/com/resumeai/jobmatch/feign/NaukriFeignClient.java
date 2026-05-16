package com.resumeai.jobmatch.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naukri", url = "${jobmatch.external.naukri.base-url:}")
public interface NaukriFeignClient {

    @GetMapping
    JsonNode fetchJobs(
            @RequestParam("keywords") String keywords,
            @RequestParam("location") String location
    );
}
