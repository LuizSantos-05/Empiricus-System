package br.com.empiricus.filaespera;

import br.com.empiricus.auth.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping({"/api/fila-espera", "/api/waitlist"})
public class FilaEsperaController {

    private final FilaEsperaService filaService;

    public FilaEsperaController(FilaEsperaService filaService) {
        this.filaService = filaService;
    }

    @GetMapping
    public List<FilaEsperaDtos.Response> listar(
            @RequestParam @Positive Long restaurantId,
            @AuthenticationPrincipal User user) {
        return filaService.listar(restaurantId, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilaEsperaDtos.Response criar(
            @RequestParam @Positive Long restaurantId,
            @Valid @RequestBody FilaEsperaDtos.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return filaService.criar(restaurantId, request, user);
    }

    @PutMapping("/{id}")
    public FilaEsperaDtos.Response atualizar(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long restaurantId,
            @Valid @RequestBody FilaEsperaDtos.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return filaService.atualizar(restaurantId, id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long restaurantId,
            @AuthenticationPrincipal User user) {
        filaService.remover(restaurantId, id, user);
    }
}
