package vn.edu.jwtjjwt.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI jwtApiDocumentation() {
        return new OpenAPI()
            .info(new Info()
                .title("JWT Authentication API — JJWT")
                .version("1.0.0")
                .description("API đăng ký, đăng nhập và truy cập tài nguyên người dùng bằng Bearer JWT."))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Đăng nhập để lấy token, sau đó nhập token tại Authorize.")));
    }
}
