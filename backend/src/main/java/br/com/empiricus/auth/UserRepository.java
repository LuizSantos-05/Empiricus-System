package br.com.empiricus.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Busca sem diferenciar letras maiusculas e minusculas no email.
    Optional<User> findByEmailIgnoreCase(String email);

    // Evita cadastrar dois usuarios com o mesmo email.
    boolean existsByEmailIgnoreCase(String email);
}