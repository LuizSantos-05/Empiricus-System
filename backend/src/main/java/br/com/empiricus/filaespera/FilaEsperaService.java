package br.com.empiricus.filaespera;

import br.com.empiricus.auth.User;
import br.com.empiricus.mesa.RestaurantAccessRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FilaEsperaService {

    private final FilaEsperaRepository filaRepository;
    private final RestaurantAccessRepository restaurantAccessRepository;

    public FilaEsperaService(
            FilaEsperaRepository filaRepository,
            RestaurantAccessRepository restaurantAccessRepository) {
        this.filaRepository = filaRepository;
        this.restaurantAccessRepository = restaurantAccessRepository;
    }

    @Transactional(readOnly = true)
    public List<FilaEsperaDtos.Response> listar(Long restaurantId, User user) {
        validarAcesso(restaurantId, user);
        return filaRepository
                .findByRestaurantIdAndStatusOrderByPosicaoAsc(
                        restaurantId, StatusFilaEspera.WAITING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FilaEsperaDtos.Response criar(
            Long restaurantId,
            FilaEsperaDtos.CreateRequest request,
            User user) {
        validarAcesso(restaurantId, user);
        validarERestringirCreate(request);
        lockFila(restaurantId);

        List<FilaEspera> fila = filaRepository
                .findByRestaurantIdAndStatusOrderByPosicaoAsc(
                        restaurantId, StatusFilaEspera.WAITING);
        normalizar(fila);

        int posicao = filaRepository.findUltimaPosicao(
                restaurantId, StatusFilaEspera.WAITING) + 1;
        FilaEspera entrada = new FilaEspera(
                restaurantId,
                request.nome().trim(),
                request.quantidadePessoas(),
                request.telefone().trim(),
                request.email().trim(),
                posicao);

        try {
            entrada = filaRepository.saveAndFlush(entrada);
            normalizar(filaRepository.findByRestaurantIdAndStatusOrderByPosicaoAsc(
                    restaurantId, StatusFilaEspera.WAITING));
            return toResponse(entrada);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nao foi possivel adicionar a pessoa a fila",
                    exception);
        }
    }

    @Transactional
    public FilaEsperaDtos.Response atualizar(
            Long restaurantId,
            Long id,
            FilaEsperaDtos.UpdateRequest request,
            User user) {
        validarAcesso(restaurantId, user);
        lockFila(restaurantId);

        if (request.nome() == null
                && request.quantidadePessoas() == null
                && request.telefone() == null
                && request.email() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe pelo menos um dado para atualizar");
        }

        String nome = validarTextoOpcional(request.nome(), "Nome invalido");
        String telefone = validarTextoOpcional(request.telefone(), "Telefone invalido");
        String email = validarTextoOpcional(request.email(), "E-mail invalido");

        FilaEspera entrada = buscar(restaurantId, id);
        entrada.atualizar(
                nome,
                request.quantidadePessoas(),
                telefone,
                email);

        try {
            return toResponse(filaRepository.saveAndFlush(entrada));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nao foi possivel atualizar a pessoa na fila",
                    exception);
        }
    }

    @Transactional
    public void remover(Long restaurantId, Long id, User user) {
        validarAcesso(restaurantId, user);
        lockFila(restaurantId);

        FilaEspera entrada = buscar(restaurantId, id);
        filaRepository.delete(entrada);
        filaRepository.flush();

        List<FilaEspera> restantes = filaRepository
                .findByRestaurantIdAndStatusOrderByPosicaoAsc(
                        restaurantId, StatusFilaEspera.WAITING);
        normalizar(restantes);
    }

    private FilaEspera buscar(Long restaurantId, Long id) {
        return filaRepository
                .findByIdAndRestaurantIdAndStatus(
                        id, restaurantId, StatusFilaEspera.WAITING)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pessoa nao encontrada na fila"));
    }

    private void validarAcesso(Long restaurantId, User user) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "restaurantId deve ser positivo");
        }
        if (!restaurantAccessRepository.hasActiveMembership(user.getId(), restaurantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario nao possui acesso ao restaurante");
        }
    }

    private void lockFila(Long restaurantId) {
        restaurantAccessRepository.lockRestaurant(restaurantId);
    }

    private void validarERestringirCreate(FilaEsperaDtos.CreateRequest request) {
        if (request.nome() == null || request.nome().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome invalido");
        }
        if (request.quantidadePessoas() == null || request.quantidadePessoas() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade de pessoas deve ser positiva");
        }
        if (request.telefone() == null || request.telefone().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone invalido");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail invalido");
        }
    }

    private String validarTextoOpcional(String valor, String mensagem) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return trimmed;
    }

    private void normalizar(List<FilaEspera> fila) {
        int deslocamento = fila.size() + 1000;
        for (FilaEspera entrada : fila) {
            entrada.definirPosicao(entrada.getPosicao() + deslocamento);
        }
        filaRepository.saveAllAndFlush(fila);

        for (int indice = 0; indice < fila.size(); indice++) {
            fila.get(indice).definirPosicao(indice + 1);
        }
        filaRepository.saveAllAndFlush(fila);
    }

    private FilaEsperaDtos.Response toResponse(FilaEspera entrada) {
        return new FilaEsperaDtos.Response(
                entrada.getId(),
                entrada.getPosicao(),
                entrada.getNome(),
                entrada.getQuantidadePessoas(),
                entrada.getPhone(),
                entrada.getEmail());
    }
}
