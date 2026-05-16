package com.resumeai.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.ai.dto.AiRequestResponse;
import com.resumeai.ai.dto.AtsCheckRequest;
import com.resumeai.ai.dto.AtsReportResponse;
import com.resumeai.ai.dto.BulletPointsRequest;
import com.resumeai.ai.dto.CoverLetterRequest;
import com.resumeai.ai.dto.ImproveSectionRequest;
import com.resumeai.ai.dto.QuotaResponse;
import com.resumeai.ai.dto.SkillsSuggestionRequest;
import com.resumeai.ai.dto.SummaryRequest;
import com.resumeai.ai.dto.TailorResumeRequest;
import com.resumeai.ai.dto.TranslateResumeRequest;
import com.resumeai.ai.entity.AiRequest;
import com.resumeai.ai.exception.AiQuotaExceededException;
import com.resumeai.ai.repository.AiRequestRepository;
import com.resumeai.ai.service.AiService;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final long MONTHLY_QUOTA = 30;
    private static final String DEFAULT_MODEL = "GPT-4o";
    private static final String WORD_EXPERIENCE = "experience";
    private static final String WORD_SKILLS = "skills";
    private static final String WORD_SUMMARY = "summary";
    private static final String WORD_DEVELOPED = "developed";
    private static final String WORD_MANAGED = "managed";
    private static final String WORD_LED = "led";
    private static final String WORD_TEAM = "team";
    private static final String WORD_PROJECT = "project";
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "from", "that", "this", "your", "you", "are", "was",
        "have", "has", "had", "will", "can", "our", "their", "them", "into", "about", "using",
        "based", "role", "job", "resume", WORD_EXPERIENCE, WORD_SKILLS, "skill", "worked", "work"
    );
    private static final List<String> SKILL_CATALOG = List.of(
            "Java", "Spring Boot", "REST APIs", "Microservices", "SQL", "MySQL", "PostgreSQL",
            "Docker", "Kubernetes", "AWS", "Azure", "CI/CD", "Git", "Testing", "JUnit",
            "JavaScript", "TypeScript", "React", "Python", "Leadership", "Communication", "Agile"
    );

    private final AiRequestRepository aiRequestRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AiRequestResponse generateSummary(SummaryRequest request) {
        String summary = buildSummary(request.resumeTitle(), request.resumeText(), request.targetRole());
        return persistAndMap(request.userId(), request.resumeId(), "SUMMARY", request.resumeText(), request.model(), summary);
    }

    @Override
    @Transactional
    public AiRequestResponse generateBulletPoints(BulletPointsRequest request) {
        String bullets = buildBullets(request.sectionName(), request.content());
        return persistAndMap(request.userId(), request.resumeId(), "BULLETS", request.content(), request.model(), bullets);
    }

    @Override
    @Transactional
    public AiRequestResponse generateCoverLetter(CoverLetterRequest request) {
        String coverLetter = buildCoverLetter(request.fullName(), request.companyName(), request.jobTitle(), request.resumeHighlights());
        return persistAndMap(request.userId(), request.resumeId(), "COVER_LETTER", request.resumeHighlights(), request.model(), coverLetter);
    }

    @Override
    @Transactional
    public AiRequestResponse improveSection(ImproveSectionRequest request) {
        String improved = improveSectionText(request.sectionName(), request.content(), request.instructions());
        String prompt = String.join("\n", request.sectionName(), request.content(), Objects.toString(request.instructions(), ""));
        return persistAndMap(request.userId(), request.resumeId(), "IMPROVE", prompt, request.model(), improved);
    }

    @Override
    @Transactional
    public AtsReportResponse checkAtsCompatibility(AtsCheckRequest request) {
        AtsAnalysis analysis = analyzeAts(request.resumeText(), request.jobDescription());
        AiRequest aiRequest = persistRecord(
                request.userId(),
                request.resumeId(),
                "ATS",
                request.resumeText() + "\n" + request.jobDescription(),
                request.model(),
                analysis.toJson()
        );
        return new AtsReportResponse(
                aiRequest.getRequestId(),
                aiRequest.getUserId(),
                aiRequest.getResumeId(),
                aiRequest.getRequestType(),
                aiRequest.getModel(),
                aiRequest.getTokensUsed(),
                aiRequest.getStatus(),
                analysis.score(),
                analysis.matchedKeywords(),
                analysis.missingKeywords(),
                analysis.recommendations(),
                aiRequest.getCreatedAt(),
                aiRequest.getCompletedAt()
        );
    }

    @Override
    @Transactional
    public AiRequestResponse suggestSkills(SkillsSuggestionRequest request) {
        List<String> suggestions = suggestSkillsInternal(request.resumeText(), request.jobDescription(), request.currentSkills());
        return persistAndMap(request.userId(), request.resumeId(), "SKILLS", request.resumeText() + "\n" + request.jobDescription(), request.model(), String.join("\n", suggestions));
    }

    @Override
    @Transactional
    public AiRequestResponse tailorForJob(TailorResumeRequest request) {
        AtsAnalysis analysis = analyzeAts(request.resumeJson(), request.jobDescription());
        Map<String, Object> tailored = new LinkedHashMap<>();
        tailored.put("tailoredSummary", buildSummary("Tailored Resume", request.resumeJson(), request.jobDescription()));
        tailored.put("optimizedKeywords", analysis.matchedKeywords());
        tailored.put("recommendations", analysis.recommendations());
        tailored.put("sourceResume", request.resumeJson());
        String tailoredJson = writeJson(tailored);
        return persistAndMap(request.userId(), request.resumeId(), "TAILOR", request.resumeJson() + "\n" + request.jobDescription(), request.model(), tailoredJson);
    }

    @Override
    @Transactional
    public AiRequestResponse translateResume(TranslateResumeRequest request) {
        String translated = translateText(request.text(), request.targetLanguage());
        return persistAndMap(request.userId(), request.resumeId(), "TRANSLATE", request.text(), request.model(), translated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiRequestResponse> getAiHistory(Long userId, Long resumeId) {
        List<AiRequest> requests = resumeId == null
                ? aiRequestRepository.findByUserId(userId)
                : aiRequestRepository.findByResumeId(resumeId).stream()
                .filter(request -> request.getUserId().equals(userId))
                .toList();
        return requests.stream()
                .sorted(Comparator.comparing(AiRequest::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuotaResponse getRemainingQuota(Long userId) {
        long used = countRequestsThisMonth(userId);
        long remaining = Math.max(MONTHLY_QUOTA - used, 0);
        long tokens = tokensUsedThisMonth(userId);
        return new QuotaResponse(userId, MONTHLY_QUOTA, used, remaining, tokens);
    }

    private AiRequestResponse persistAndMap(Long userId,
                                            Long resumeId,
                                            String requestType,
                                            String inputPrompt,
                                            String model,
                                            String responseText) {
        AiRequest aiRequest = persistRecord(userId, resumeId, requestType, inputPrompt, model, responseText);
        return mapToResponse(aiRequest);
    }

    private AiRequest persistRecord(Long userId,
                                    Long resumeId,
                                    String requestType,
                                    String inputPrompt,
                                    String model,
                                    String responseText) {
        enforceQuota(userId);
        String resolvedModel = resolveModel(model);
        LocalDateTime now = LocalDateTime.now();
        AiRequest aiRequest = AiRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .userId(userId)
                .resumeId(resumeId)
                .requestType(normalizeRequestType(requestType))
                .inputPrompt(inputPrompt)
                .aiResponse(responseText)
                .model(resolvedModel)
                .tokensUsed(estimateTokens(inputPrompt, responseText))
                .status("COMPLETED")
                .createdAt(now)
                .completedAt(now)
                .build();
        return aiRequestRepository.save(aiRequest);
    }

    private void enforceQuota(Long userId) {
        if (countRequestsThisMonth(userId) >= MONTHLY_QUOTA) {
            throw new AiQuotaExceededException(userId);
        }
    }

    private long countRequestsThisMonth(Long userId) {
        YearMonth yearMonth = YearMonth.now();
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        return aiRequestRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
    }

    private long tokensUsedThisMonth(Long userId) {
        YearMonth yearMonth = YearMonth.now();
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        return aiRequestRepository.sumTokensByUserIdBetween(userId, start, end);
    }

    private AiRequestResponse mapToResponse(AiRequest aiRequest) {
        return new AiRequestResponse(
                aiRequest.getRequestId(),
                aiRequest.getUserId(),
                aiRequest.getResumeId(),
                aiRequest.getRequestType(),
                aiRequest.getModel(),
                aiRequest.getTokensUsed(),
                aiRequest.getStatus(),
                aiRequest.getAiResponse(),
                aiRequest.getCreatedAt(),
                aiRequest.getCompletedAt()
        );
    }

    private String resolveModel(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return DEFAULT_MODEL;
        }
        String normalized = requestedModel.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("CLAUDE")) {
            return "CLAUDE";
        }
        if (normalized.equals("GPT-4O") || normalized.equals("GPT-4O MINI") || normalized.equals("GPT-4O-MINI") || normalized.equals("GPT-4O".toUpperCase(Locale.ROOT))) {
            return DEFAULT_MODEL;
        }
        throw new IllegalArgumentException("Unsupported AI model: " + requestedModel);
    }

    private String normalizeRequestType(String requestType) {
        return requestType.trim().toUpperCase(Locale.ROOT);
    }

    private int estimateTokens(String inputPrompt, String responseText) {
        int inputTokens = Math.max(1, inputPrompt.length() / 4);
        int outputTokens = Math.max(1, responseText.length() / 4);
        return inputTokens + outputTokens;
    }

    private String buildSummary(String resumeTitle, String resumeText, String targetRole) {
        List<String> sentences = splitSentences(resumeText);
        String firstSentence = sentences.isEmpty() ? resumeText.trim() : sentences.get(0).trim();
        String secondSentence = sentences.size() > 1 ? sentences.get(1).trim() : "";
        StringBuilder builder = new StringBuilder();
        builder.append("Professional summary for ").append(resumeTitle).append('.');
        if (targetRole != null && !targetRole.isBlank()) {
            builder.append(' ').append("Targeting ").append(targetRole.trim()).append('.');
        }
        builder.append(' ').append(firstSentence);
        if (!secondSentence.isBlank()) {
            builder.append(' ').append(secondSentence);
        }
        return builder.toString().trim();
    }

    private String buildBullets(String sectionName, String content) {
        List<String> parts = splitSentences(content);
        if (parts.isEmpty()) {
            parts = List.of(content.trim());
        }
        StringBuilder builder = new StringBuilder();
        builder.append(sectionName).append(':').append('\n');
        for (String part : parts.stream().limit(5).toList()) {
            if (!part.isBlank()) {
                builder.append("- ").append(capitalizeSentence(part.trim())).append('\n');
            }
        }
        return builder.toString().trim();
    }

    private String buildCoverLetter(String fullName, String companyName, String jobTitle, String highlights) {
        return String.join("\n\n",
                "Dear Hiring Team at " + companyName + ",",
                "I am writing to express interest in the " + jobTitle + " role. " + capitalizeSentence(highlights),
                "My background and achievements make me a strong fit for this opportunity.",
                "Sincerely,",
                fullName);
    }

    private String improveSectionText(String sectionName, String content, String instructions) {
        String normalizedContent = capitalizeSentence(content.trim().replaceAll("\\s+", " "));
        String improvementNote = instructions == null || instructions.isBlank()
                ? ""
                : "\nImprovement focus: " + instructions.trim();
        return sectionName + "\n" + normalizedContent + improvementNote;
    }

    private AtsAnalysis analyzeAts(String resumeText, String jobDescription) {
        Set<String> resumeKeywords = extractKeywords(resumeText);
        Set<String> jobKeywords = extractKeywords(jobDescription);
        List<String> matched = jobKeywords.stream()
                .filter(resumeKeywords::contains)
                .sorted()
                .toList();
        List<String> missing = jobKeywords.stream()
                .filter(keyword -> !resumeKeywords.contains(keyword))
                .sorted()
                .toList();
        int score = jobKeywords.isEmpty() ? 0 : (int) Math.round((matched.size() * 100.0) / jobKeywords.size());
        List<String> recommendations = new ArrayList<>();
        if (score < 70) {
            recommendations.add("Add more keywords from the target job description.");
        }
        if (missing.stream().anyMatch(keyword -> keyword.contains("lead"))) {
            recommendations.add("Emphasize leadership or ownership examples.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("The resume is aligned with the target role.");
        }
        return new AtsAnalysis(score, matched, missing, recommendations);
    }

    private List<String> suggestSkillsInternal(String resumeText, String jobDescription, List<String> currentSkills) {
        Set<String> current = currentSkills == null
                ? Set.of()
                : currentSkills.stream().map(this::normalizeSkill).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> resumeKeywords = extractKeywords(resumeText);
        Set<String> jobKeywords = extractKeywords(jobDescription);
        List<String> suggestions = new ArrayList<>();
        for (String skill : SKILL_CATALOG) {
            String normalizedSkill = normalizeSkill(skill);
            boolean relevant = resumeKeywords.contains(normalizedSkill) || jobKeywords.contains(normalizedSkill);
            if (relevant && !current.contains(normalizedSkill) && suggestions.stream().noneMatch(existing -> normalizeSkill(existing).equals(normalizedSkill))) {
                suggestions.add(skill);
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Add measurable impact metrics.");
            suggestions.add("Highlight tools and technologies used most recently.");
        }
        return suggestions.stream().limit(8).toList();
    }

    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9+#]+"))
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .map(this::normalizeSkill)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String translateText(String text, String targetLanguage) {
        String language = targetLanguage.trim().toLowerCase(Locale.ROOT);
        Map<String, String> dictionary = translationDictionary(language);
        if (dictionary.isEmpty()) {
            return targetLanguage + ": " + text;
        }
        String translated = text;
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            translated = translated.replaceAll("(?i)\\b" + Pattern.quote(entry.getKey()) + "\\b", Matcher.quoteReplacement(entry.getValue()));
        }
        return translated;
    }

    private Map<String, String> translationDictionary(String language) {
        return switch (language) {
            case "spanish", "es", "esp" -> Map.of(
                WORD_EXPERIENCE, "experiencia",
                WORD_SKILLS, "habilidades",
                WORD_SUMMARY, "resumen",
                WORD_DEVELOPED, "desarrolló",
                WORD_MANAGED, "gestionó",
                WORD_LED, "lideró",
                WORD_TEAM, "equipo",
                WORD_PROJECT, "proyecto"
            );
            case "french", "fr" -> Map.of(
                WORD_EXPERIENCE, "expérience",
                WORD_SKILLS, "compétences",
                WORD_SUMMARY, "résumé",
                WORD_DEVELOPED, "a développé",
                WORD_MANAGED, "a géré",
                WORD_LED, "a dirigé",
                WORD_TEAM, "équipe",
                WORD_PROJECT, "projet"
            );
            case "german", "de" -> Map.of(
                WORD_EXPERIENCE, "Erfahrung",
                WORD_SKILLS, "Fähigkeiten",
                WORD_SUMMARY, "Zusammenfassung",
                WORD_DEVELOPED, "entwickelte",
                WORD_MANAGED, "verwaltete",
                WORD_LED, "leitete",
                WORD_TEAM, "Team",
                WORD_PROJECT, "Projekt"
            );
            default -> Map.of();
        };
    }

    private String normalizeSkill(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String capitalizeSentence(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    private List<String> splitSentences(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("(?<=[.!?])\\s+|\\n+"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ignored) {
            throw new IllegalStateException("Unable to serialize tailored resume response");
        }
    }

    private record AtsAnalysis(int score,
                               List<String> matchedKeywords,
                               List<String> missingKeywords,
                               List<String> recommendations) {

        private String toJson() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException exception) {
                return "{}";
            }
        }
    }

    @Override
    public Long getTotalUsageCount() {
        return aiRequestRepository.count();
    }

    @Override
    public Double getTotalCost() {
        Long totalTokens = aiRequestRepository.sumTotalTokens();
        // Calculate cost: assume $0.002 per 1K tokens (typical OpenAI pricing)
        return totalTokens != null ? (totalTokens * 0.002) / 1000.0 : 0.0;
    }
}
