package br.com.empiricus.mesa;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Integer capacity;

    @Convert(converter = MesaStatusConverter.class)
    @Column(nullable = false)
    private MesaStatus status;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Mesa() {
    }

    public Mesa(Long restaurantId, String tableNumber, Integer capacity, MesaStatus status) {
        this.restaurantId = restaurantId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public MesaStatus getStatus() {
        return status;
    }

    public void update(Integer capacity, MesaStatus status) {
        if (capacity != null) {
            this.capacity = capacity;
        }
        if (status != null) {
            this.status = status;
        }
    }
}
