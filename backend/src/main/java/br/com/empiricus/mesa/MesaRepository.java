package br.com.empiricus.mesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByRestaurantIdAndActiveTrueAndStatusNotOrderByIdAsc(
            Long restaurantId,
            MesaStatus status);

    Optional<Mesa> findByIdAndRestaurantIdAndActiveTrueAndStatusNot(
            Long id,
            Long restaurantId,
            MesaStatus status);

    long countByRestaurantIdAndActiveTrueAndStatusNot(Long restaurantId, MesaStatus status);
}
