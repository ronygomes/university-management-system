package me.ronygomes.ums.api.config;

import me.ronygomes.ums.api.helper.HalContentFilter;
import me.ronygomes.ums.api.helper.HalDataExcluder;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

@Configuration
public class WebConfig {

    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        SimpleFilterProvider fp = new SimpleFilterProvider();
        fp.addFilter(HalDataExcluder.FILTER_NAME, new HalContentFilter());

        return builder -> builder.filterProvider(fp);
    }
}
