package com.agrofinance.config;
 
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
/**
 * Registers the JWT bearer security scheme so Swagger UI shows an
 * "Authorize" button. Without this, springdoc still documents every
 * endpoint automatically, but there'd be no way to attach a token —
 * every protected endpoint would return 401 when tried from the UI.
 *
 * Everything else (endpoints, request/response schemas, validation
 * constraints) is auto-generated from the existing controllers, DTOs,
 * and Bean Validation annotations — no work needed.
 */
@Configuration
public class OpenApiConfig {
 
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
 
    @Bean
    public OpenAPI agroFinanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgroFinance API")
                        .description("AI-powered agricultural credit intelligence platform. "
                                + "Authenticate via /api/auth/login, then click Authorize and paste the token.")
                        .version("v1.0")
                        .contact(new Contact().name("AgroFinance")))
                // Applies the scheme globally so the Authorize button's token
                // is sent on every request the UI makes.
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste ONLY the token — Swagger adds the 'Bearer ' prefix itself.")));
    }
 
}
 