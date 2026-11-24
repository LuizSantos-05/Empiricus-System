package br.com.empiricus.mesa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MesaStatusConverter implements AttributeConverter<StatusMesa, String> {

    @Override
    // Converte o status da entidade para o nome compatível armazenado no banco de dados.
    public String convertToDatabaseColumn(StatusMesa status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case LIVRE -> "FREE";
            case OCUPADO -> "OCCUPIED";
            case RESERVADO -> "RESERVED";
            case EM_ESPERA -> "WAITING";
            case INATIVA -> "INACTIVE";
        };
    }

    @Override
    // Converte o texto armazenado no banco para o status correspondente da entidade.
    public StatusMesa convertToEntityAttribute(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "FREE" -> StatusMesa.LIVRE;
            case "OCCUPIED" -> StatusMesa.OCUPADO;
            case "RESERVED" -> StatusMesa.RESERVADO;
            case "WAITING" -> StatusMesa.EM_ESPERA;
            case "INACTIVE" -> StatusMesa.INATIVA;
            default -> throw new IllegalArgumentException("Status de mesa desconhecido: " + status);
        };
    }
}
