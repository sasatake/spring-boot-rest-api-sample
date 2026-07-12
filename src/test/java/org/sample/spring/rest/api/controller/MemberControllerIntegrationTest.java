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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberControllerIntegrationTest {

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
	void createMemberReturnsCreatedWithLocation() throws Exception {
		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田太郎", "taro@example.com", "090-1234-5678")))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.name").value("山田太郎"))
				.andExpect(jsonPath("$.email").value("taro@example.com"))
				.andExpect(jsonPath("$.phone").value("090-1234-5678"))
				.andExpect(jsonPath("$.registeredAt").isNotEmpty());
	}

	@Test
	void createMemberWithInvalidBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("", "not-an-email", null)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors", hasSize(2)));
	}

	@Test
	void createMemberWithDuplicateEmailReturnsConflict() throws Exception {
		insertMember("山田太郎", "taro@example.com");

		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田次郎", "taro@example.com", null)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void listMembersReturnsRegisteredAtDescendingPage() throws Exception {
		jdbcTemplate.update(
				"INSERT INTO members (name, email, registered_at) VALUES ('先に登録', 'first@example.com', '2026-01-01 10:00:00')");
		jdbcTemplate.update(
				"INSERT INTO members (name, email, registered_at) VALUES ('後に登録', 'second@example.com', '2026-02-01 10:00:00')");

		mockMvc.perform(get("/members"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].name").value("後に登録"))
				.andExpect(jsonPath("$.content[1].name").value("先に登録"));

		mockMvc.perform(get("/members").param("page", "1").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].name").value("先に登録"))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void getMemberReturnsMemberOrNotFound() throws Exception {
		long memberId = insertMember("山田太郎", "taro@example.com");

		mockMvc.perform(get("/members/" + memberId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("taro@example.com"));

		mockMvc.perform(get("/members/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void updateMemberReplacesFieldsKeepingRegisteredAt() throws Exception {
		long memberId = insertMember("山田太郎", "taro@example.com");
		String registeredAt = jdbcTemplate.queryForObject(
				"SELECT registered_at::text FROM members WHERE id = ?", String.class, memberId);

		mockMvc.perform(put("/members/" + memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田太郎(改姓)", "taro-new@example.com", "080-0000-0000")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("山田太郎(改姓)"))
				.andExpect(jsonPath("$.email").value("taro-new@example.com"));

		assertThat(jdbcTemplate.queryForObject(
				"SELECT registered_at::text FROM members WHERE id = ?", String.class, memberId))
				.isEqualTo(registeredAt);
	}

	@Test
	void updateMemberKeepingOwnEmailSucceeds() throws Exception {
		long memberId = insertMember("山田太郎", "taro@example.com");

		mockMvc.perform(put("/members/" + memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田太郎", "taro@example.com", "090-1234-5678")))
				.andExpect(status().isOk());
	}

	@Test
	void updateMemberWithDuplicateEmailReturnsConflict() throws Exception {
		insertMember("山田太郎", "taro@example.com");
		long memberId = insertMember("山田次郎", "jiro@example.com");

		mockMvc.perform(put("/members/" + memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田次郎", "taro@example.com", null)))
				.andExpect(status().isConflict());

		mockMvc.perform(put("/members/999")
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田次郎", "new@example.com", null)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteMemberSoftDeletesAndKeepsLoanHistory() throws Exception {
		long memberId = insertMember("山田太郎", "taro@example.com");
		long bookId = insertBook("吾輩は猫である", "978-4-10-101035-9");
		jdbcTemplate.update(
				"INSERT INTO loans (book_id, member_id, due_date, returned_at) VALUES (?, ?, CURRENT_DATE, CURRENT_TIMESTAMP)",
				bookId, memberId);

		mockMvc.perform(delete("/members/" + memberId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/members/" + memberId))
				.andExpect(status().isNotFound());

		// レコードは deleted_at 付きで残り、貸出履歴も保持される
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM members WHERE id = ? AND deleted_at IS NOT NULL", Integer.class, memberId)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM loans WHERE member_id = ?", Integer.class, memberId)).isEqualTo(1);
	}

	@Test
	void deleteMemberWithActiveLoanReturnsConflict() throws Exception {
		long memberId = insertMember("山田太郎", "taro@example.com");
		long bookId = insertBook("吾輩は猫である", "978-4-10-101035-9");
		jdbcTemplate.update(
				"INSERT INTO loans (book_id, member_id, due_date) VALUES (?, ?, CURRENT_DATE + 14)", bookId, memberId);

		mockMvc.perform(delete("/members/" + memberId))
				.andExpect(status().isConflict());

		jdbcTemplate.update("UPDATE loans SET returned_at = CURRENT_TIMESTAMP WHERE member_id = ?", memberId);

		mockMvc.perform(delete("/members/" + memberId))
				.andExpect(status().isNoContent());
	}

	@Test
	void emailOfDeletedMemberCanBeReused() throws Exception {
		long memberId = insertMember("山田太郎", "taro@example.com");

		mockMvc.perform(delete("/members/" + memberId))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(memberJson("山田太郎(再入会)", "taro@example.com", null)))
				.andExpect(status().isCreated());
	}

	@Test
	void deleteMemberReturnsNotFoundForUnknownId() throws Exception {
		mockMvc.perform(delete("/members/999"))
				.andExpect(status().isNotFound());
	}

	private long insertMember(String name, String email) {
		return jdbcTemplate.queryForObject(
				"INSERT INTO members (name, email) VALUES (?, ?) RETURNING id", Long.class, name, email);
	}

	private long insertBook(String title, String isbn) {
		return jdbcTemplate.queryForObject(
				"INSERT INTO books (title, isbn) VALUES (?, ?) RETURNING id", Long.class, title, isbn);
	}

	private String memberJson(String name, String email, String phone) {
		var body = new java.util.LinkedHashMap<String, Object>();
		body.put("name", name);
		body.put("email", email);
		body.put("phone", phone);
		try {
			return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}
