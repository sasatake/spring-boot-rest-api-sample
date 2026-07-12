package org.sample.spring.rest.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI libraryOpenApi() {
		return new OpenAPI().info(new Info()
				.title("図書館管理システム API")
				.description("書籍・著者・カテゴリ・会員・貸出を管理する REST API")
				.version("1.0.0"));
	}
}
