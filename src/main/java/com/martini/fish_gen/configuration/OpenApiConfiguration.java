package com.martini.fish_gen.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    public static final String BEARER_JWT_SCHEME = "bearer-jwt";

    // Every endpoint requires the JWT by default; public ones opt out with an empty @SecurityRequirements
    @Bean
    OpenAPI fishGenOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fish-gen API")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT_SCHEME));
    }
}
