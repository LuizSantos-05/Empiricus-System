package br.com.empiricus.mesa;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class MesaDtos {

    // Impede instanciar esta classe usada exclusivamente como agrupador de DTOs.
    private MesaDtos() {
    }

    public record CreateRequest(
            @NotNull @Positive Long restaurantId,
            @NotBlank @Size(max = 30) String tableNumber,
            @NotNull @Min(1) @Max(65535) Integer capacidade,
            @NotNull StatusMesa status) {
    }

    public record UpdateRequest(
            @Min(1) @Max(65535) Integer capacidade,
            StatusMesa status) {
    }

    public record Response(Long id, StatusMesa status, Integer capacidade) {
    }
}
