package com.resumeai.auth.repository;

import com.resumeai.auth.entity.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(Long userId);

    boolean existsByEmail(String email);

    List<UserAccount> findAllByRole(String role);

    List<UserAccount> findBySubscriptionPlan(String subscriptionPlan);

    List<UserAccount> findByIsActive(boolean isActive);

    void deleteById(Long userId);
}
