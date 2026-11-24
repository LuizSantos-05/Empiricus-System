package br.com.empiricus.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SessionService(
            UserSessionRepository sessionRepository,
            UserRepository userRepository,
            JwtService jwtService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        String token = jwtService.generateToken(user, sessionId);
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.save(new UserSession(
                sessionId,
                user,
                hashToken(token),
                now,
                now.plusNanos(jwtService.getExpirationMs() * 1_000_000)));
        return new AuthDtos.AuthResponse(token, "Bearer", jwtService.getExpirationMs());
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String token) {
        String sessionId = jwtService.extractSessionId(token);
        UserSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null
                || !session.isActiveAt(LocalDateTime.now())
                || !matchesToken(token, session.getTokenHash())) {
            return Optional.empty();
        }

        User user = userRepository.findById(session.getUser().getId()).orElse(null);
        if (user == null || !jwtService.isValid(token, user, sessionId)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Transactional(readOnly = true)
    public List<AuthDtos.SessionResponse> listSessions(User user, String authorizationHeader) {
        String currentSessionId = sessionIdFromAuthorization(authorizationHeader);
        return sessionRepository
                .findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        user.getId(), LocalDateTime.now())
                .stream()
                .map(session -> new AuthDtos.SessionResponse(
                        session.getId(),
                        session.getCreatedAt(),
                        session.getExpiresAt(),
                        session.getId().equals(currentSessionId)))
                .toList();
    }

    @Transactional
    public void revokeCurrentSession(User user, String authorizationHeader) {
        String token = bearerToken(authorizationHeader);
        revokeSession(user, jwtService.extractSessionId(token), token);
    }

    @Transactional
    public void revokeSession(User user, String sessionId) {
        UserSession session = ownedSession(user, sessionId);
        session.revoke(LocalDateTime.now());
    }

    @Transactional
    public void revokeAllSessions(User user) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        user.getId(), now)
                .forEach(session -> session.revoke(now));
    }

    private void revokeSession(User user, String sessionId, String token) {
        UserSession session = ownedSession(user, sessionId);
        if (!matchesToken(token, session.getTokenHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        }
        session.revoke(LocalDateTime.now());
    }

    private UserSession ownedSession(User user, String sessionId) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessao nao encontrada"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessao nao encontrada");
        }
        return session;
    }

    private String sessionIdFromAuthorization(String authorizationHeader) {
        return jwtService.extractSessionId(bearerToken(authorizationHeader));
    }

    private String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Bearer obrigatorio");
        }
        return authorizationHeader.substring(7);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 nao esta disponivel", exception);
        }
    }

    private boolean matchesToken(String token, String expectedHash) {
        byte[] actual = hashToken(token).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }
}
