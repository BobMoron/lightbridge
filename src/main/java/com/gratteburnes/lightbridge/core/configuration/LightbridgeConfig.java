package com.gratteburnes.lightbridge.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gratteburnes.magichome.service.DiscoveryService;
import com.gratteburnes.magichome.service.MessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableRetry
public class LightbridgeConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public DiscoveryService discoveryService() {
        return new DiscoveryService();
    }

    @Bean
    public MessageService messageService() {
        return new MessageService();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/lightbridge/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "PUT", "DELETE");
            }
        };
    }
}
