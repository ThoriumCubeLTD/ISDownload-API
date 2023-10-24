package com.infernalsuite.isdownloadapi.configuration;

import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
class WebConfiguration {
    @Bean
    Filter shallowETagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
