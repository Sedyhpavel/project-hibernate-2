package com.javarush.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RatingConverter implements AttributeConverter<Rating, String> {
    @Override
    public String convertToDatabaseColumn(Rating attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Rating convertToEntityAttribute(String dbData) {
        return Rating.fromValue(dbData);
    }
}
