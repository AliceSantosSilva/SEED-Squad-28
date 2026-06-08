package com.projeto.sistema_escolar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger / OpenAPI.
 * Acesse em: http://localhost:8081/swagger-ui.html
 *
 * Para testar rotas protegidas no Swagger:
 * 1. Faça POST /api/login e copie o campo "token" da resposta
 * 2. Clique em "Authorize" (cadeado) no topo da página
 * 3. Cole o token no campo e clique em Authorize
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("SEED — Sistema de Gestão de Provas")
                .description("API da Secretaria de Educação de Sergipe")
                .version("1.0.0")
            )
            // Adiciona o campo JWT em todas as rotas protegidas
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .components(new Components()
                .addSecuritySchemes(schemeName, new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                )
            );
    }
}