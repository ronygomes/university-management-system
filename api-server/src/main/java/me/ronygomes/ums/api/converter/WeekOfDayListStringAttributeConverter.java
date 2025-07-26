package me.ronygomes.ums.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Converter
public class WeekOfDayListStringAttributeConverter
        implements AttributeConverter<List<DayOfWeek>, String> {

    @Override
    public String convertToDatabaseColumn(List<DayOfWeek> attribute) {
        if (Objects.isNull(attribute)  || attribute.isEmpty()) {
            return null;
        }

        return attribute.stream().map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<DayOfWeek> convertToEntityAttribute(String dbData) {
        List<DayOfWeek> result = new ArrayList<>();
        if (Objects.isNull(dbData)) {
            return result;
        }

        String[] parts = dbData.split(",");
        for (String part : parts) {
            try {
                result.add(DayOfWeek.valueOf(part));
            } catch (IllegalArgumentException ignore) {
            }
        }

        return result;
    }
}
