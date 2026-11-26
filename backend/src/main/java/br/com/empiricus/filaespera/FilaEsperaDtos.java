package br.com.empiricus.filaespera;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class FilaEsperaDtos {

    private FilaEsperaDtos() {
    }

    public record CreateRequest(
            @JsonAlias({"name", "customerName", "nomeResponsavel"})
            @NotBlank @Size(max = 150) String nome,
            @JsonAlias({"partySize", "quantidade"})
            @NotNull @Positive @Max(65535) Integer quantidadePessoas,
            @JsonAlias({"phone", "telefoneContato"})
            @NotBlank @Size(max = 30)
            @Pattern(regexp = "^[0-9+(). -]+$", message = "Telefone invalido")
            String telefone,
            @JsonAlias({"emailContato"})
            @NotBlank @Email @Size(max = 190) String email) {
    }

    public record UpdateRequest(
            @JsonAlias({"name", "customerName", "nomeResponsavel"})
            @Size(max = 150) String nome,
            @JsonAlias({"partySize", "quantidade"})
            @Positive @Max(65535) Integer quantidadePessoas,
            @JsonAlias({"phone", "telefoneContato"})
            @Size(max = 30)
            @Pattern(regexp = "^[0-9+(). -]+$", message = "Telefone invalido")
            String telefone,
            @JsonAlias({"emailContato"})
            @Email @Size(max = 190) String email) {
    }

    public record Response(
            Long id,
            Integer posicao,
            String nome,
            Integer quantidadePessoas,
            String telefone,
            String email) {
    }
}
