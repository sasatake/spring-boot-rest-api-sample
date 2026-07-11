package org.sample.spring.rest.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class AuthorControllerIntegrationTest {

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
	void createAuthorReturnsCreatedWithLocation() throws Exception {
		mockMvc.perform(post("/authors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "夏目漱石"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("夏目漱石"));
	}

	@Test
	void createAuthorWithBlankNameReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/authors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": ""}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[0].field").value("name"));
	}

	@Test
	void listAuthorsReturnsNameOrderedPage() throws Exception {
		insertAuthor("森鴎外");
		insertAuthor("夏目漱石");
		insertAuthor("芥川龍之介");

		mockMvc.perform(get("/authors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(3)))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.content[0].name").value("夏目漱石"))
				.andExpect(jsonPath("$.content[1].name").value("森鴎外"))
				.andExpect(jsonPath("$.content[2].name").value("芥川龍之介"));

		mockMvc.perform(get("/authors").param("page", "1").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void getAuthorReturnsAuthorOrNotFound() throws Exception {
		long authorId = insertAuthor("夏目漱石");

		mockMvc.perform(get("/authors/" + authorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("夏目漱石"));

		mockMvc.perform(get("/authors/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void updateAuthorReplacesName() throws Exception {
		long authorId = insertAuthor("夏目漱石");

		mockMvc.perform(put("/authors/" + authorId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "夏目金之助"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("夏目金之助"));

		mockMvc.perform(put("/authors/999")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "夏目金之助"}
						"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteAuthorRemovesAuthor() throws Exception {
		long authorId = insertAuthor("夏目漱石");

		mockMvc.perform(delete("/authors/" + authorId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/authors/" + authorId))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/authors/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteAuthorReferencedByBookReturnsConflict() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long bookId = jdbcTemplate.queryForObject(
				"INSERT INTO books (title, isbn) VALUES ('吾輩は猫である', '978-4-10-101035-9') RETURNING id", Long.class);
		jdbcTemplate.update("INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)", bookId, authorId);

		mockMvc.perform(delete("/authors/" + authorId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	private long insertAuthor(String name) {
		return jdbcTemplate.queryForObject("INSERT INTO authors (name) VALUES (?) RETURNING id", Long.class, name);
	}
}
