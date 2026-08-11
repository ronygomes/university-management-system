package me.ronygomes.ums.api.helper;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;

public class HalContentFilter extends SimpleBeanPropertyFilter {

    private static final String ERROR_MESSAGE = "%s must implement " + HalDataExcluder.class.getName();

    @Override
    public void serializeAsProperty(Object halDto, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception {
        if (!(halDto instanceof HalDataExcluder excluder)) {
            throw new IllegalArgumentException(String.format(ERROR_MESSAGE, halDto.getClass().getName()));
        }

        if (excluder.include(excluder.displayType(), writer.getName())) {
            writer.serializeAsProperty(halDto, jgen, provider);
        }
    }
}
