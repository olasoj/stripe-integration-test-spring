package com.payment.stripeintegration.security.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.payment.stripeintegration.security.xss.XSSSanitizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonConverter());
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1", HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Bean
    public HttpMessageConverter<Object> jsonConverter() {
        var module = new SimpleModule();
        module.addDeserializer(String.class, new XSSSanitizer());

        var objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.registerModule(module);

        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
