package org.sample.spring.rest.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void cleanUp() {
		jdbcTemplate.execute(
				"TRUNCATE TABLE loans, book_authors, book_categories, books, authors, categories, members RESTART IDENTITY CASCADE");
	}

	@Test
	void createBookReturnsCreatedWithLocation() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long categoryId = insertCategory("小説");

		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("吾輩は猫である", "978-4-10-101035-9", 1905, authorId, categoryId)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.title").value("吾輩は猫である"))
				.andExpect(jsonPath("$.isbn").value("978-4-10-101035-9"))
				.andExpect(jsonPath("$.publishedYear").value(1905))
				.andExpect(jsonPath("$.authors[0].name").value("夏目漱石"))
				.andExpect(jsonPath("$.categories[0].name").value("小説"));
	}

	@Test
	void createBookWithInvalidBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "", "isbn": "invalid-isbn", "authorIds": []}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors", hasSize(3)));
	}

	@Test
	void createBookWithFutureYearReturnsBadRequest() throws Exception {
		long authorId = insertAuthor("夏目漱石");

		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("未来の本", "978-4-10-101035-9", 9999, authorId, null)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("publishedYear"));
	}

	@Test
	void createBookWithUnknownAuthorReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("吾輩は猫である", "978-4-10-101035-9", 1905, 999L, null)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("authorIds"))
				.andExpect(jsonPath("$.errors[0].message").value("not found: id=999"));
	}

	@Test
	void createBookWithDuplicateIsbnReturnsConflict() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		createBook("吾輩は猫である", "978-4-10-101035-9", authorId, null);

		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("坊っちゃん", "978-4-10-101035-9", 1906, authorId, null)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void listBooksFiltersByTitleAndCategoryWithPaging() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long novelId = insertCategory("小説");
		long essayId = insertCategory("随筆");
		createBook("吾輩は猫である", "978-4-10-101035-9", authorId, novelId);
		createBook("坊っちゃん", "978-4-10-101004-5", authorId, novelId);
		createBook("硝子戸の中", "978-4-00-310116-8", authorId, essayId);

		mockMvc.perform(get("/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(3)))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(1));

		mockMvc.perform(get("/books").param("title", "猫"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title").value("吾輩は猫である"));

		mockMvc.perform(get("/books").param("categoryId", String.valueOf(novelId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.totalElements").value(2));

		mockMvc.perform(get("/books").param("page", "1").param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void getBookReturnsNotFoundForUnknownId() throws Exception {
		mockMvc.perform(get("/books/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void updateBookReplacesFieldsAndRelations() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long newAuthorId = insertAuthor("森鴎外");
		long categoryId = insertCategory("小説");
		long bookId = createBook("吾輩は猫である", "978-4-10-101035-9", authorId, categoryId);

		mockMvc.perform(put("/books/" + bookId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("舞姫", "978-4-00-310501-2", 1890, newAuthorId, null)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("舞姫"))
				.andExpect(jsonPath("$.isbn").value("978-4-00-310501-2"))
				.andExpect(jsonPath("$.authors", hasSize(1)))
				.andExpect(jsonPath("$.authors[0].name").value("森鴎外"))
				.andExpect(jsonPath("$.categories", hasSize(0)));
	}

	@Test
	void updateBookWithDuplicateIsbnReturnsConflict() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		createBook("吾輩は猫である", "978-4-10-101035-9", authorId, null);
		long bookId = createBook("坊っちゃん", "978-4-10-101004-5", authorId, null);

		mockMvc.perform(put("/books/" + bookId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("坊っちゃん", "978-4-10-101035-9", 1906, authorId, null)))
				.andExpect(status().isConflict());
	}

	@Test
	void updateBookKeepingOwnIsbnSucceeds() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long bookId = createBook("吾輩は猫である", "978-4-10-101035-9", authorId, null);

		mockMvc.perform(put("/books/" + bookId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("吾輩は猫である(改版)", "978-4-10-101035-9", 1905, authorId, null)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("吾輩は猫である(改版)"));
	}

	@Test
	void updateBookReturnsNotFoundForUnknownId() throws Exception {
		long authorId = insertAuthor("夏目漱石");

		mockMvc.perform(put("/books/999")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("吾輩は猫である", "978-4-10-101035-9", 1905, authorId, null)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteBookSoftDeletesAndHidesFromApi() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long categoryId = insertCategory("小説");
		long bookId = createBook("吾輩は猫である", "978-4-10-101035-9", authorId, categoryId);

		mockMvc.perform(delete("/books/" + bookId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/books/" + bookId))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(0));

		// レコード自体は deleted_at 付きで残り、関連も保持される
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM books WHERE id = ? AND deleted_at IS NOT NULL", Integer.class, bookId)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM book_authors WHERE book_id = ?", Integer.class, bookId)).isEqualTo(1);
	}

	@Test
	void isbnOfDeletedBookCanBeReused() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long bookId = createBook("吾輩は猫である", "978-4-10-101035-9", authorId, null);

		mockMvc.perform(delete("/books/" + bookId))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson("吾輩は猫である(新装版)", "978-4-10-101035-9", 1905, authorId, null)))
				.andExpect(status().isCreated());
	}

	@Test
	void deleteBookOnLoanReturnsConflict() throws Exception {
		long authorId = insertAuthor("夏目漱石");
		long bookId = createBook("吾輩は猫である", "978-4-10-101035-9", authorId, null);
		long memberId = jdbcTemplate.queryForObject(
				"INSERT INTO members (name, email) VALUES ('山田太郎', 'taro@example.com') RETURNING id", Long.class);
		jdbcTemplate.update(
				"INSERT INTO loans (book_id, member_id, due_date) VALUES (?, ?, CURRENT_DATE + 14)", bookId, memberId);

		mockMvc.perform(delete("/books/" + bookId))
				.andExpect(status().isConflict());

		jdbcTemplate.update("UPDATE loans SET returned_at = CURRENT_TIMESTAMP WHERE book_id = ?", bookId);

		mockMvc.perform(delete("/books/" + bookId))
				.andExpect(status().isNoContent());

		// 貸出履歴は削除後も監査証跡として保持される
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM loans WHERE book_id = ?", Integer.class, bookId)).isEqualTo(1);
	}

	@Test
	void deleteBookReturnsNotFoundForUnknownId() throws Exception {
		mockMvc.perform(delete("/books/999"))
				.andExpect(status().isNotFound());
	}

	private long insertAuthor(String name) {
		return jdbcTemplate.queryForObject("INSERT INTO authors (name) VALUES (?) RETURNING id", Long.class, name);
	}

	private long insertCategory(String name) {
		return jdbcTemplate.queryForObject("INSERT INTO categories (name) VALUES (?) RETURNING id", Long.class, name);
	}

	private String bookJson(String title, String isbn, Integer publishedYear, Long authorId, Long categoryId)
			throws Exception {
		var body = new java.util.LinkedHashMap<String, Object>();
		body.put("title", title);
		body.put("isbn", isbn);
		body.put("publishedYear", publishedYear);
		body.put("authorIds", java.util.List.of(authorId));
		body.put("categoryIds", categoryId == null ? java.util.List.of() : java.util.List.of(categoryId));
		return objectMapper.writeValueAsString(body);
	}

	private long createBook(String title, String isbn, Long authorId, Long categoryId) throws Exception {
		MvcResult result = mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bookJson(title, isbn, 1905, authorId, categoryId)))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}
}
