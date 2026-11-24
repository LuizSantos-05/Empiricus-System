package br.com.empiricus.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Chave usada para assinar e verificar todos os tokens emitidos pela aplicacao.
    private final SecretKey signingKey;
    // Tempo de validade configurado em milissegundos.
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String base64Secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs) {
        // A chave fica em Base64 nas propriedades para facilitar o uso por variaveis de ambiente.
        this.signingKey = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user, String sessionId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        // O ID da sessao permite revogar o token sem armazenar o JWT em texto puro.
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("sessionId", sessionId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String extractSessionId(String token) {
        return parseClaims(token).get("sessionId", String.class);
    }

    public boolean isValid(String token, User user, String sessionId) {
        try {
            Claims claims = parseClaims(token);
            // A assinatura, o usuario, a expiracao e o status ativo precisam ser validos.
            return claims.getSubject().equalsIgnoreCase(user.getEmail())
                    && claims.get("userId", String.class).equals(user.getId().toString())
                    && claims.get("sessionId", String.class).equals(sessionId)
                    && claims.getExpiration().after(new Date())
                    && user.isActive();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {
        // A biblioteca rejeita tokens adulterados, expirados ou assinados com outra chave.
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}