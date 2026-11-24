package br.com.empiricus.mesa;

import br.com.empiricus.auth.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final RestaurantAccessRepository restaurantAccessRepository;

    // Inicializa o serviço com os repositórios utilizados nas operações de mesa.
    public MesaService(
            MesaRepository mesaRepository,
            RestaurantAccessRepository restaurantAccessRepository) {
        this.mesaRepository = mesaRepository;
        this.restaurantAccessRepository = restaurantAccessRepository;
    }

    @Transactional(readOnly = true)
    // Valida o acesso e lista as mesas ativas, excluindo as mesas inativas.
    public List<MesaDtos.Response> listar(Long restaurantId, User user) {
        validarAcesso(restaurantId, user);
        return mesaRepository.findByRestaurantIdAndActiveTrueAndStatusNotOrderByIdAsc(
                        restaurantId, StatusMesa.INATIVA)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    // Valida o acesso e retorna os dados da mesa solicitada.
    public MesaDtos.Response obter(Long restaurantId, Long id, User user) {
        validarAcesso(restaurantId, user);
        return toResponse(buscarMesa(restaurantId, id));
    }

    @Transactional(readOnly = true)
    // Valida o acesso e conta as mesas ativas que não estão inativas.
    public long quantidade(Long restaurantId, User user) {
        validarAcesso(restaurantId, user);
        return mesaRepository.countByRestaurantIdAndActiveTrueAndStatusNot(
                restaurantId, StatusMesa.INATIVA);
    }

    @Transactional
    // Valida o acesso e cria uma nova mesa, retornando um conflito se o número já existir.
    public MesaDtos.Response criar(MesaDtos.CreateRequest request, User user) {
        validarAcesso(request.restaurantId(), user);
        Mesa mesa = new Mesa(
                request.restaurantId(),
                request.tableNumber().trim(),
                request.capacidade(),
                request.status());
        try {
            return toResponse(mesaRepository.saveAndFlush(mesa));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ja existe uma mesa com esse numero no restaurante", exception);
        }
    }

    @Transactional
    // Valida os dados, atualiza os campos informados e persiste a mesa existente.
    public MesaDtos.Response atualizar(
            Long restaurantId,
            Long id,
            MesaDtos.UpdateRequest request,
            User user) {
        validarAcesso(restaurantId, user);
        if (request.capacidade() == null && request.status() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe a capacidade ou o status para atualizar");
        }

        Mesa mesa = buscarMesa(restaurantId, id);
        mesa.update(
                request.capacidade(),
                request.status());
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    // Valida o acesso e remove a mesa, impedindo a exclusão quando houver registros vinculados.
    public void remover(Long restaurantId, Long id, User user) {
        validarAcesso(restaurantId, user);
        Mesa mesa = buscarMesa(restaurantId, id);
        try {
            mesaRepository.delete(mesa);
            mesaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A mesa possui registros vinculados e nao pode ser removida",
                    exception);
        }
    }

    // Localiza uma mesa ativa e não inativa pelo identificador e pelo restaurante.
    private Mesa buscarMesa(Long restaurantId, Long id) {
        return mesaRepository
                .findByIdAndRestaurantIdAndActiveTrueAndStatusNot(
                        id, restaurantId, StatusMesa.INATIVA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa nao encontrada"));
    }

    // Verifica se o identificador é válido e se o usuário possui vínculo ativo com o restaurante.
    private void validarAcesso(Long restaurantId, User user) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "restaurantId deve ser positivo");
        }
        if (!restaurantAccessRepository.hasActiveMembership(user.getId(), restaurantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario nao possui acesso ao restaurante");
        }
    }

    // Converte uma entidade Mesa para o formato de resposta da API.
    private MesaDtos.Response toResponse(Mesa mesa) {
        return new MesaDtos.Response(
                mesa.getId(),
                mesa.getStatus(),
                mesa.getCapacity());
    }
}
