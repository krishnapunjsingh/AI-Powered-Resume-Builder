package com.resumeai.jobmatch.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.resumeai.jobmatch.dto.JobListingResponse;
import com.resumeai.jobmatch.feign.LinkedInFeignClient;
import com.resumeai.jobmatch.feign.NaukriFeignClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalJobPortalClient {

    private final LinkedInFeignClient linkedInFeignClient;
    private final NaukriFeignClient naukriFeignClient;

    @Value("${jobmatch.external.stub-enabled:true}")
    private boolean stubEnabled;

    @Value("${jobmatch.external.linkedin.base-url:}")
    private String linkedInBaseUrl;

    @Value("${jobmatch.external.naukri.base-url:}")
    private String naukriBaseUrl;

    public List<JobListingResponse> fetchLinkedInJobs(String keywords, String location) {
        return fetchJobs(linkedInBaseUrl, "LINKEDIN", keywords, location);
    }

    public List<JobListingResponse> fetchNaukriJobs(String keywords, String location) {
        return fetchJobs(naukriBaseUrl, "NAUKRI", keywords, location);
    }

    private List<JobListingResponse> fetchJobs(String baseUrl,
                                               String source,
                                               String keywords,
                                               String location) {
        if (stubEnabled || baseUrl == null || baseUrl.isBlank()) {
            return stubJobs(source, keywords, location);
        }

        try {
            JsonNode response;
            if ("LINKEDIN".equals(source)) {
                response = linkedInFeignClient.fetchJobs(keywords, location);
            } else if ("NAUKRI".equals(source)) {
                response = naukriFeignClient.fetchJobs(keywords, location);
            } else {
                return stubJobs(source, keywords, location);
            }

            List<JobListingResponse> jobs = mapResponse(response, source);
            return jobs.isEmpty() ? stubJobs(source, keywords, location) : jobs;
        } catch (Exception exception) {
            return stubJobs(source, keywords, location);
        }
    }

    private List<JobListingResponse> mapResponse(JsonNode response, String source) {
        List<JobListingResponse> jobs = new ArrayList<>();
        if (response == null || response.isNull()) {
            return jobs;
        }

        JsonNode arrayNode = response.isArray() ? response : firstArray(response);
        if (arrayNode == null || !arrayNode.isArray()) {
            return jobs;
        }

        arrayNode.forEach(item -> jobs.add(new JobListingResponse(
                readText(item, "title", "jobTitle", "name", "position"),
                readText(item, "company", "companyName", "employer"),
                readText(item, "location", "jobLocation", "place"),
                readText(item, "url", "link", "applyUrl"),
                source
        )));

        return jobs;
    }

    private JsonNode firstArray(JsonNode response) {
        for (String fieldName : List.of("jobs", "data", "results", "items")) {
            JsonNode node = response.get(fieldName);
            if (node != null && node.isArray()) {
                return node;
            }
        }
        return null;
    }

    private String readText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return "";
    }

    private List<JobListingResponse> stubJobs(String source, String keywords, String location) {
        String normalizedKeywords = normalizeKey(keywords, "Java Developer");
        String normalizedLocation = normalizeKey(location, "Remote");
        return List.of(
                new JobListingResponse(normalizedKeywords + " Engineer", source + " Labs", normalizedLocation, "https://example.com/" + source.toLowerCase(Locale.ROOT) + "/1", source),
                new JobListingResponse(normalizedKeywords + " Specialist", "NextGen Hiring", normalizedLocation, "https://example.com/" + source.toLowerCase(Locale.ROOT) + "/2", source),
                new JobListingResponse("Senior " + normalizedKeywords, "BridgeLab Careers", normalizedLocation, "https://example.com/" + source.toLowerCase(Locale.ROOT) + "/3", source)
        );
    }

    private String normalizeKey(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String[] words = value.trim().toLowerCase(Locale.ROOT).split("\\s+");
        return Arrays.stream(words)
                .filter(token -> !token.isBlank())
                .map(token -> Character.toUpperCase(token.charAt(0)) + token.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(fallback);
    }
}