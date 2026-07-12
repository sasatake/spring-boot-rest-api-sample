package org.sample.spring.rest.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsListsAllResourcePaths() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("図書館管理システム API"))
				.andExpect(jsonPath("$.paths./books").exists())
				.andExpect(jsonPath("$.paths./books/{id}").exists())
				.andExpect(jsonPath("$.paths./authors").exists())
				.andExpect(jsonPath("$.paths./categories").exists())
				.andExpect(jsonPath("$.paths./members").exists())
				.andExpect(jsonPath("$.paths./loans").exists())
				.andExpect(jsonPath("$.paths./loans/{id}/return").exists())
				.andExpect(jsonPath("$.paths./loans/overdue").exists());
	}

	@Test
	void swaggerUiIsAvailable() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}
}
