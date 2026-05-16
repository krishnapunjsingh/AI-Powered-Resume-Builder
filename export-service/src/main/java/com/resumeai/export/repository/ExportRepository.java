package com.resumeai.export.repository;

import com.resumeai.export.entity.ExportJob;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportRepository extends JpaRepository<ExportJob, Long> {

    Optional<ExportJob> findByJobId(String jobId);

    List<ExportJob> findByUserId(Long userId);

    List<ExportJob> findByResumeId(Long resumeId);

    List<ExportJob> findByStatus(String status);

    List<ExportJob> findByFormat(String format);

    @Query("SELECT e FROM ExportJob e WHERE e.expiresAt < :now AND e.status = 'COMPLETED'")
    List<ExportJob> findExpiredJobs(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM ExportJob e WHERE e.userId = :userId AND DATE(e.requestedAt) = CAST(:today as date)")
    Long countByUserIdToday(@Param("userId") Long userId, @Param("today") LocalDateTime today);

    @Query("SELECT COUNT(e) FROM ExportJob e WHERE e.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(e) FROM ExportJob e WHERE e.format = :format")
    Long countByFormat(@Param("format") String format);
}
