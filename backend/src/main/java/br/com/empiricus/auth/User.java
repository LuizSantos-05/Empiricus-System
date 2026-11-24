package br.com.empiricus.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    // Identificador gerado pelo MySQL para cada usuario.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome exibido no sistema; a coluna e obrigatoria no banco.
    @Column(nullable = false, length = 150)
    private String name;

    // O email e a identificacao usada no login e deve ser unico.
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    // Armazena somente o hash BCrypt, nunca a senha original.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Usuarios inativos nao podem gerar ou validar autenticacoes.
    @Column(nullable = false)
    private boolean active = true;

    // O valor e preenchido pelo default CURRENT_TIMESTAMP do MySQL.
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Construtor exigido pelo JPA para reconstruir entidades vindas do banco.
    protected User() {
    }

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }
}