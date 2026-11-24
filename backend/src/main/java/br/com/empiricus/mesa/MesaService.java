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

    public MesaService(
            MesaRepository mesaRepository,
            RestaurantAccessRepository restaurantAccessRepository) {
        this.mesaRepository = mesaRepository;
        this.restaurantAccessRepository = restaurantAccessRepository;
    }

    @Transactional(readOnly = true)
    public List<MesaDtos.Response> listar(Long restaurantId, User user) {
        validarAcesso(restaurantId, user);
        return mesaRepository.findByRestaurantIdAndActiveTrueAndStatusNotOrderByIdAsc(
                        restaurantId, MesaStatus.INACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MesaDtos.Response obter(Long restaurantId, Long id, User user) {
        validarAcesso(restaurantId, user);
        return toResponse(buscarMesa(restaurantId, id));
    }

    @Transactional(readOnly = true)
    public long quantidade(Long restaurantId, User user) {
        validarAcesso(restaurantId, user);
        return mesaRepository.countByRestaurantIdAndActiveTrueAndStatusNot(
                restaurantId, MesaStatus.INACTIVE);
    }

    @Transactional
    public MesaDtos.Response criar(MesaDtos.CreateRequest request, User user) {
        validarAcesso(request.restaurantId(), user);
        Mesa mesa = new Mesa(
                request.restaurantId(),
                request.tableNumber().trim(),
                request.capacidade(),
                toEntityStatus(request.status()));
        try {
            return toResponse(mesaRepository.saveAndFlush(mesa));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ja existe uma mesa com esse numero no restaurante", exception);
        }
    }

    @Transactional
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
                request.status() == null ? null : toEntityStatus(request.status()));
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
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

    private Mesa buscarMesa(Long restaurantId, Long id) {
        return mesaRepository
                .findByIdAndRestaurantIdAndActiveTrueAndStatusNot(
                        id, restaurantId, MesaStatus.INACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa nao encontrada"));
    }

    private void validarAcesso(Long restaurantId, User user) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "restaurantId deve ser positivo");
        }
        if (!restaurantAccessRepository.hasActiveMembership(user.getId(), restaurantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario nao possui acesso ao restaurante");
        }
    }

    private MesaDtos.Response toResponse(Mesa mesa) {
        return new MesaDtos.Response(
                mesa.getId(),
                toApiStatus(mesa.getStatus()),
                mesa.getCapacity());
    }

    private MesaStatus toEntityStatus(StatusMesa status) {
        return switch (status) {
            case LIVRE -> MesaStatus.FREE;
            case OCUPADO -> MesaStatus.OCCUPIED;
            case RESERVADO -> MesaStatus.RESERVED;
            case EM_ESPERA -> MesaStatus.WAITING;
        };
    }

    private StatusMesa toApiStatus(MesaStatus status) {
        return switch (status) {
            case FREE -> StatusMesa.LIVRE;
            case OCCUPIED -> StatusMesa.OCUPADO;
            case RESERVED -> StatusMesa.RESERVADO;
            case WAITING -> StatusMesa.EM_ESPERA;
            case INACTIVE -> throw new IllegalStateException("Mesas inativas nao devem ser retornadas");
        };
    }
}
