package me.ronygomes.ums.api.converter;

import jakarta.persistence.AttributeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeekOfDayListStringAttributeConverterTest {

    private AttributeConverter<List<DayOfWeek>, String> converter;

    @BeforeEach
    void setup() {
        this.converter = new WeekOfDayListStringAttributeConverter();
    }


    @Test
    void testConvertToDatabaseColumn() {
        Assertions.assertNull(converter.convertToDatabaseColumn(null));
        Assertions.assertNull(converter.convertToDatabaseColumn(Collections.emptyList()));
        Assertions.assertEquals("SUNDAY,MONDAY",
                converter.convertToDatabaseColumn(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)));
    }

    @Test
    void testConvertToEntityAttribute() {
        Assertions.assertIterableEquals(Collections.emptyList(), converter.convertToEntityAttribute(null));
        Assertions.assertIterableEquals(Collections.emptyList(), converter.convertToEntityAttribute(""));
        Assertions.assertIterableEquals(Collections.emptyList(), converter.convertToEntityAttribute("1,2"));
        Assertions.assertIterableEquals(List.of(DayOfWeek.SUNDAY), converter.convertToEntityAttribute("SUNDAY"));
        Assertions.assertIterableEquals(List.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY),
                converter.convertToEntityAttribute("SUNDAY,SOMEDAY,MONDAY"));
    }
}
