package br.com.empiricus.filaespera;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilaEsperaRepository extends JpaRepository<FilaEspera, Long> {

    List<FilaEspera> findByRestaurantIdAndStatusOrderByPosicaoAsc(
            Long restaurantId,
            StatusFilaEspera status);

    Optional<FilaEspera> findByIdAndRestaurantIdAndStatus(
            Long id,
            Long restaurantId,
            StatusFilaEspera status);

    @Query("""
            select coalesce(max(entry.posicao), 0)
            from FilaEspera entry
            where entry.restaurantId = :restaurantId
              and entry.status = :status
            """)
    int findUltimaPosicao(
            @Param("restaurantId") Long restaurantId,
            @Param("status") StatusFilaEspera status);
}
