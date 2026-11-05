package br.com.jobhunter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI jobHunterOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Job Hunter API")
                        .description("API de monitoramento de oportunidades, compatibilidade e candidaturas.")
                        .version("v1")
                        .contact(new Contact().name("Job Hunter"))
                )
                .servers(List.of(new Server().url("/").description("Servidor atual")))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, bearer));
    }
}
