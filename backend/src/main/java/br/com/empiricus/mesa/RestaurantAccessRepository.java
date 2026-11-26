package br.com.empiricus.mesa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantAccessRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Verifica se o usuário possui associação ativa em um restaurante também ativo.
    public boolean hasActiveMembership(Long userId, Long restaurantId) {
        Number membershipCount = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM user_restaurants ur
                JOIN restaurants r ON r.id = ur.restaurant_id
                WHERE ur.user_id = :userId
                  AND ur.restaurant_id = :restaurantId
                  AND ur.active = TRUE
                  AND r.active = TRUE
                """)
                .setParameter("userId", userId)
                .setParameter("restaurantId", restaurantId)
                .getSingleResult();
        return membershipCount.longValue() > 0;
    }

    public void lockRestaurant(Long restaurantId) {
        entityManager.createNativeQuery("""
                SELECT id
                FROM restaurants
                WHERE id = :restaurantId
                  AND active = TRUE
                FOR UPDATE
                """)
                .setParameter("restaurantId", restaurantId)
                .getResultList();
    }
}
