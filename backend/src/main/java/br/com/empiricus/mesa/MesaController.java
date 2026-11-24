package br.com.empiricus.mesa;

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
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public List<MesaDtos.Response> listar(
            @RequestParam @Positive Long restaurantId,
            @AuthenticationPrincipal User user) {
        return mesaService.listar(restaurantId, user);
    }

    @GetMapping("/quantidade")
    public long quantidade(
            @RequestParam @Positive Long restaurantId,
            @AuthenticationPrincipal User user) {
        return mesaService.quantidade(restaurantId, user);
    }

    @GetMapping("/{id}")
    public MesaDtos.Response obter(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long restaurantId,
            @AuthenticationPrincipal User user) {
        return mesaService.obter(restaurantId, id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MesaDtos.Response criar(
            @Valid @RequestBody MesaDtos.CreateRequest request,
            @AuthenticationPrincipal User user) {
        return mesaService.criar(request, user);
    }

    @PutMapping("/{id}")
    public MesaDtos.Response atualizar(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long restaurantId,
            @Valid @RequestBody MesaDtos.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        return mesaService.atualizar(restaurantId, id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long restaurantId,
            @AuthenticationPrincipal User user) {
        mesaService.remover(restaurantId, id, user);
    }
}
