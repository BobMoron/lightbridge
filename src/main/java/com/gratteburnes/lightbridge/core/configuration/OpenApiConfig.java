package com.gratteburnes.lightbridge.core.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Autowired(required = false) BuildProperties buildProperties) {
        String version = buildProperties != null ? buildProperties.getVersion() : "N/A";
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Magic home protocol bridge").description(
                        "This is a back-end service allowing for control of connected magic home devices")
                .version(version));
    }
}