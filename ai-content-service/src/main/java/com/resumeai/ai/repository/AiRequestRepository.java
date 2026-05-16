package com.resumeai.ai.repository;

import com.resumeai.ai.entity.AiRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiRequestRepository extends JpaRepository<AiRequest, String> {

    List<AiRequest> findByUserId(Long userId);

    List<AiRequest> findByResumeId(Long resumeId);

    Optional<AiRequest> findByRequestId(String requestId);

    List<AiRequest> findByRequestType(String requestType);

    List<AiRequest> findByStatus(String status);

    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    default long countByUserIdToday(Long userId) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return countByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Query("select coalesce(sum(a.tokensUsed), 0) from AiRequest a where a.userId = :userId")
    Long sumTokensByUserId(@Param("userId") Long userId);

    @Query("select coalesce(sum(a.tokensUsed), 0) from AiRequest a where a.userId = :userId and a.createdAt between :start and :end")
    Long sumTokensByUserIdBetween(@Param("userId") Long userId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("select coalesce(sum(a.tokensUsed), 0) from AiRequest a")
    Long sumTotalTokens();

    @Query("select coalesce(sum(a.tokensUsed), 0) from AiRequest a where a.createdAt between :start and :end")
    Long sumTokensBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long count();
}
