package br.com.empiricus.filaespera;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist_entries")
public class FilaEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(name = "party_size", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer quantidadePessoas;

    @Column(name = "position", nullable = false)
    private Integer posicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFilaEspera status = StatusFilaEspera.WAITING;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected FilaEspera() {
    }

    public FilaEspera(
            Long restaurantId,
            String nome,
            Integer quantidadePessoas,
            String phone,
            String email,
            Integer posicao) {
        this.restaurantId = restaurantId;
        this.nome = nome;
        this.quantidadePessoas = quantidadePessoas;
        this.phone = phone;
        this.email = email;
        this.posicao = posicao;
        this.status = StatusFilaEspera.WAITING;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getNome() {
        return nome;
    }

    public Integer getQuantidadePessoas() {
        return quantidadePessoas;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Integer getPosicao() {
        return posicao;
    }

    public StatusFilaEspera getStatus() {
        return status;
    }

    public void atualizar(String nome, Integer quantidadePessoas, String phone, String email) {
        if (nome != null) {
            this.nome = nome;
        }
        if (quantidadePessoas != null) {
            this.quantidadePessoas = quantidadePessoas;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (email != null) {
            this.email = email;
        }
    }

    public void definirPosicao(Integer posicao) {
        this.posicao = posicao;
    }
}
