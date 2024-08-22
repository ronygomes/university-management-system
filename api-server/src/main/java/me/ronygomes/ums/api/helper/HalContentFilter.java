package me.ronygomes.ums.api.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class HalContentFilter extends SimpleBeanPropertyFilter {

    private static final String ERROR_MESSAGE = "%s must implement " + HalDataExcluder.class.getName();

    @Override
    public void serializeAsField(Object halDto, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (!(halDto instanceof HalDataExcluder excluder)) {
            throw new IllegalArgumentException(String.format(ERROR_MESSAGE, halDto.getClass().getName()));
        }

        if (excluder.include(excluder.displayType(), writer.getName())) {
            writer.serializeAsField(halDto, jgen, provider);
        }
    }
}
