package br.com.empiricus.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        // Normalizar evita que o mesmo email seja cadastrado com variacoes de caixa ou espacos.
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            // Trata uma tentativa de cadastrar um endereco de email que ja pertence a outro usuario.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        }

        // BCrypt cria um hash com salt; a senha original nao e persistida.
        User user;
        // Trata cadastro concorrente que tenta gravar um email ja existente apesar da verificacao previa.
        try {
            user = userRepository.saveAndFlush(new User(
                    request.name().trim(),
                    email,
                    passwordEncoder.encode(request.password())));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado", exception);
        }
        return sessionService.createSession(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Retornar a mesma mensagem nos dois erros evita revelar se o email existe.
        // Trata email inexistente ou usuario inativo sem revelar qual condicao falhou.
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou senha invalidos"));

        // Trata senha incorreta usando a mesma resposta da tentativa com email invalido.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos");
        }

        return sessionService.createSession(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}