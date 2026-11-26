package br.com.empiricus.filaespera;

import br.com.empiricus.auth.User;
import br.com.empiricus.mesa.RestaurantAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilaEsperaServiceTest {

    private FilaEsperaRepository repository;
    private RestaurantAccessRepository accessRepository;
    private FilaEsperaService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(FilaEsperaRepository.class);
        accessRepository = mock(RestaurantAccessRepository.class);
        service = new FilaEsperaService(repository, accessRepository);
        user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(accessRepository.hasActiveMembership(7L, 1L)).thenReturn(true);
    }

    @Test
    void createAddsAtEndOfQueue() {
        FilaEspera entrada = new FilaEspera(
                1L, "Ana", 3, "(45) 99999-0000", "ana@example.com", 1);

        when(repository.findByRestaurantIdAndStatusOrderByPosicaoAsc(
                1L, StatusFilaEspera.WAITING)).thenReturn(List.of());
        when(repository.findUltimaPosicao(1L, StatusFilaEspera.WAITING)).thenReturn(0);
        when(repository.saveAndFlush(any(FilaEspera.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FilaEsperaDtos.Response response = service.criar(
                1L,
                new FilaEsperaDtos.CreateRequest(
                        "Ana", 3, "(45) 99999-0000", "ana@example.com"),
                user);

        assertEquals(1, response.posicao());
    }

    @Test
    void updateOfMissingEntryReturnsNotFound() {
        when(repository.findByIdAndRestaurantIdAndStatus(
                99L, 1L, StatusFilaEspera.WAITING)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.atualizar(
                1L,
                99L,
                new FilaEsperaDtos.UpdateRequest(
                        "Novo nome", null, null, null),
                user));
    }

    @Test
    void removeReindexesRemainingEntries() {
        FilaEspera primeiro = new FilaEspera(
                1L, "Ana", 2, "4511111111", "ana@example.com", 1);
        FilaEspera removido = new FilaEspera(
                1L, "Bia", 3, "4522222222", "bia@example.com", 2);
        FilaEspera terceiro = new FilaEspera(
                1L, "Caio", 4, "4533333333", "caio@example.com", 3);

        when(repository.findByIdAndRestaurantIdAndStatus(
                eq(2L), eq(1L), eq(StatusFilaEspera.WAITING)))
                .thenReturn(Optional.of(removido));
        when(repository.findByRestaurantIdAndStatusOrderByPosicaoAsc(
                1L, StatusFilaEspera.WAITING)).thenReturn(
                        new ArrayList<>(List.of(primeiro, terceiro)));

        service.remover(1L, 2L, user);

        assertEquals(1, primeiro.getPosicao());
        assertEquals(2, terceiro.getPosicao());
    }
}
