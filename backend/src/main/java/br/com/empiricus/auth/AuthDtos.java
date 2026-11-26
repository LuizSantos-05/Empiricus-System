package br.com.empiricus.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

        // Dados recebidos no cadastro; as anotacoes fazem a validacao antes do service.
    public record RegisterRequest(
            @NotBlank(message = "Nome e obrigatorio")
            @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
            String name,
            @NotBlank(message = "Email e obrigatorio")
            @Email(message = "Email invalido")
            @Size(max = 190, message = "O email deve ter no maximo 190 caracteres")
            String email,
            @NotBlank(message = "Senha e obrigatoria")
            @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres")
            String password) {
    }

        // Dados recebidos no login; a senha tambem precisa respeitar o minimo definido.
    public record LoginRequest(
            @NotBlank(message = "Email e obrigatorio")
            @Email(message = "Email invalido")
            String email,
            @NotBlank(message = "Senha e obrigatoria")
            @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres")
            String password) {
    }

        // Resposta compartilhada por cadastro e login. O token deve ser enviado como Bearer.
    public record AuthResponse(String token, String tokenType, long expiresIn) {
    }

        // Resposta segura da rota /me, sem expor o hash da senha.
        public record UserResponse(Long id, String name, String email) {
        }

    public record SessionResponse(String id, LocalDateTime createdAt, LocalDateTime expiresAt, boolean current) {
    }
}