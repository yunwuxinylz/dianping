package com.dp.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("点评系统 API")
                                                .description("点评系统后端API文档")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("开发团队")
                                                                .email("123456789@qq.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                                .components(new Components()
                                                .addSecuritySchemes("Authorization", new SecurityScheme()
                                                                .name("Authorization")
                                                                .type(SecurityScheme.Type.APIKEY)
                                                                .in(SecurityScheme.In.HEADER)
                                                                .description("使用JWT token进行认证，格式为{token}")));
        }

        @Bean
        public GroupedOpenApi publicApi() {
                return GroupedOpenApi.builder()
                                .group("public")
                                .pathsToMatch("/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi shopApi() {
                return GroupedOpenApi.builder()
                                .group("shop")
                                .pathsToMatch("/shop/**")
                                .build();
        }

        @Bean
        public GroupedOpenApi userApi() {
                return GroupedOpenApi.builder()
                                .group("user")
                                .pathsToMatch("/user/**")
                                .build();
        }
}