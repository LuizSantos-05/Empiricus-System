package br.com.empiricus.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    List<UserSession> findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime now);
}
