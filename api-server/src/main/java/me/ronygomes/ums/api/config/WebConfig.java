package me.ronygomes.ums.api.config;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import me.ronygomes.ums.api.helper.HalContentFilter;
import me.ronygomes.ums.api.helper.HalDataExcluder;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        SimpleFilterProvider fp = new SimpleFilterProvider();
        fp.addFilter(HalDataExcluder.FILTER_NAME, new HalContentFilter());

        return builder -> builder.filters(fp);
    }
}
