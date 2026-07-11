package org.sample.spring.rest.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanUp() {
		jdbcTemplate.execute(
				"TRUNCATE TABLE loans, book_authors, book_categories, books, authors, categories, members RESTART IDENTITY CASCADE");
	}

	@Test
	void createCategoryReturnsCreatedWithLocation() throws Exception {
		mockMvc.perform(post("/categories")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "小説"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("小説"));
	}

	@Test
	void createCategoryWithBlankNameReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/categories")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": ""}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[0].field").value("name"));
	}

	@Test
	void listCategoriesReturnsNameOrderedArray() throws Exception {
		insertCategory("随筆");
		insertCategory("小説");

		mockMvc.perform(get("/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].name").value("小説"))
				.andExpect(jsonPath("$[1].name").value("随筆"));
	}

	@Test
	void deleteCategoryRemovesCategory() throws Exception {
		long categoryId = insertCategory("小説");

		mockMvc.perform(delete("/categories/" + categoryId))
				.andExpect(status().isNoContent());

		mockMvc.perform(delete("/categories/" + categoryId))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteCategoryReferencedByBookReturnsConflict() throws Exception {
		long categoryId = insertCategory("小説");
		long bookId = jdbcTemplate.queryForObject(
				"INSERT INTO books (title, isbn) VALUES ('吾輩は猫である', '978-4-10-101035-9') RETURNING id", Long.class);
		jdbcTemplate.update("INSERT INTO book_categories (book_id, category_id) VALUES (?, ?)", bookId, categoryId);

		mockMvc.perform(delete("/categories/" + categoryId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	private long insertCategory(String name) {
		return jdbcTemplate.queryForObject("INSERT INTO categories (name) VALUES (?) RETURNING id", Long.class, name);
	}
}
