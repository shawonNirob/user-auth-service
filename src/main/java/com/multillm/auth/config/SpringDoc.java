package com.multillm.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringDoc {
    @Bean
    public OpenAPI openAPI() {
        log.debug("Configuring OpenAPI bean");
        return new OpenAPI()
                .info(new Info()
                        .title("Org Flow API")
                        .description("API documentation for Org Flow application")
                        .version("1.0"));
    }
}
