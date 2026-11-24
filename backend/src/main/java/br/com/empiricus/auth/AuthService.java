package br.com.empiricus.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        // Normalizar evita que o mesmo email seja cadastrado com variacoes de caixa ou espacos.
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        }

        // BCrypt cria um hash com salt; a senha original nao e persistida.
        User user = userRepository.save(new User(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password())));
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Retornar a mesma mensagem nos dois erros evita revelar se o email existe.
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou senha invalidos"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos");
        }

        return responseFor(user);
    }

    private AuthDtos.AuthResponse responseFor(User user) {
        // O JWT e devolvido ao cliente e sera enviado no header Authorization.
        return new AuthDtos.AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationMs());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}