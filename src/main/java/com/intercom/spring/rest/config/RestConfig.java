package com.intercom.spring.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@Configuration
public class RestConfig implements WebMvcConfigurer {

  @Bean
  public ObjectMapper jacksonObjectMapper(final Jackson2ObjectMapperBuilder builder) {
    return builder
        .modulesToInstall(new ParameterNamesModule(), new ProblemModule(), new ConstraintViolationProblemModule())
        .createXmlMapper(false)
        .failOnUnknownProperties(false)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new ByteArrayHttpMessageConverter());
  }
}

