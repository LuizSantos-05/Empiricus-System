package br.com.empiricus.mesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    // Busca todas as mesas ativas de um restaurante que não estejam inativas, em ordem crescente.
    List<Mesa> findByRestaurantIdAndActiveTrueAndStatusNotOrderByIdAsc(
            Long restaurantId,
            StatusMesa status);

    // Busca uma mesa ativa e não inativa pelo identificador da mesa e do restaurante.
    Optional<Mesa> findByIdAndRestaurantIdAndActiveTrueAndStatusNot(
            Long id,
            Long restaurantId,
            StatusMesa status);

    // Conta as mesas ativas de um restaurante que não estejam com status inativo.
    long countByRestaurantIdAndActiveTrueAndStatusNot(Long restaurantId, StatusMesa status);
}
