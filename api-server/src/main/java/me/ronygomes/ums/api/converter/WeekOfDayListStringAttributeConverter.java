package me.ronygomes.ums.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class WeekOfDayListStringAttributeConverter
        implements AttributeConverter<List<DayOfWeek>, String> {

    @Override
    public String convertToDatabaseColumn(List<DayOfWeek> attribute) {
        return attribute.stream().map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<DayOfWeek> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(","))
                .map(DayOfWeek::valueOf)
                .toList();
    }
}
