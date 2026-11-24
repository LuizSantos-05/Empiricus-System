package br.com.empiricus.mesa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MesaStatusConverter implements AttributeConverter<MesaStatus, String> {

    @Override
    public String convertToDatabaseColumn(MesaStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public MesaStatus convertToEntityAttribute(String status) {
        return status == null ? null : MesaStatus.valueOf(status);
    }
}
