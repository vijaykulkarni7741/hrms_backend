package com.webicSoftIndia.HRMS.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_KEY = "BearerAuth";
    private static final String SESSION_KEY = "SessionIdHeader";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HRMS Management API")
                        .version("1.0")
                        .description("HRMS REST API with JWT Bearer Token and Session ID Header Security"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(BEARER_KEY)
                        .addList(SESSION_KEY))
                .components(new Components()
                        .addSecuritySchemes(BEARER_KEY, new SecurityScheme()
                                .name(BEARER_KEY)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Token"))
                        .addSecuritySchemes(SESSION_KEY, new SecurityScheme()
                                .name("X-Session-ID")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Enter Session ID")));
    }
}
