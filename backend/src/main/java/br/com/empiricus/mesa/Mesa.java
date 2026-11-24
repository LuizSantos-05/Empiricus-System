package br.com.empiricus.mesa;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dining_tables")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "table_number", nullable = false, length = 30)
    private String tableNumber;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private Integer capacity;

    @Convert(converter = MesaStatusConverter.class)
    @Column(nullable = false)
    private StatusMesa status;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;


    // Construtor protegido exigido pelo JPA para instanciar a entidade
    // quando os dados são carregados do banco de dados.

    protected Mesa() {
    }

    // Cria uma mesa associada a um restaurante, com número, capacidade e
    // situação informados; a mesa é criada inicialmente como ativa.

    public Mesa(Long restaurantId, String tableNumber, Integer capacity, StatusMesa status) {
        this.restaurantId = restaurantId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.active = true;
    }

    // Retorna o identificador único da mesa gerado pelo banco de dados.

    public Long getId() {
        return id;
    }

    // Retorna o identificador do restaurante ao qual esta mesa pertence.

    public Long getRestaurantId() {
        return restaurantId;
    }

    // Retorna a capacidade máxima de ocupantes da mesa.

    public Integer getCapacity() {
        return capacity;
    }

    // Retorna a situação atual da mesa.

    public StatusMesa getStatus() {
        return status;
    }

    // Atualiza a capacidade e/ou a situação da mesa apenas quando os
    // respectivos valores informados não forem nulos.

    public void update(Integer capacity, StatusMesa status) {
        if (capacity != null) {
            this.capacity = capacity;
        }
        if (status != null) {
            this.status = status;
        }
    }
}
