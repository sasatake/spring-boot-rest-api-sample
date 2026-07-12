package org.sample.spring.rest.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * フレームワーク層で発生するエラー(不正 JSON、型不一致、未知パスなど)が
 * common.md の共通エラー形式で返ることを検証する。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorHandlingIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void malformedJsonBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{invalid json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("malformed request body"));
	}

	@Test
	void typeMismatchInBodyFieldReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "t", "isbn": "978-4-10-101035-9", "publishedYear": "not-a-number", "authorIds": [1]}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void pathVariableTypeMismatchReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/books/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[0].field").value("id"));
	}

	@Test
	void queryParamTypeMismatchReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/books").param("categoryId", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[0].field").value("categoryId"));
	}

	@Test
	void unknownPathReturnsNotFound() throws Exception {
		mockMvc.perform(get("/unknown-path"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
		mockMvc.perform(patch("/books"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.status").value(405));
	}

	@Test
	void unsupportedMediaTypeReturnsCommonFormat() throws Exception {
		mockMvc.perform(post("/books")
				.contentType(MediaType.TEXT_PLAIN)
				.content("plain text"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.status").value(415));
	}
}
