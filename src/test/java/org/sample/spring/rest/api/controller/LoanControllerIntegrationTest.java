package org.sample.spring.rest.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanControllerIntegrationTest {

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
	void createLoanReturnsCreatedWithDueDate14DaysLater() throws Exception {
		long bookId = insertBook("吾輩は猫である", "978-4-10-101035-9");
		long memberId = insertMember("山田太郎", "taro@example.com");

		mockMvc.perform(post("/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loanJson(bookId, memberId)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.book.title").value("吾輩は猫である"))
				.andExpect(jsonPath("$.member.name").value("山田太郎"))
				.andExpect(jsonPath("$.loanedAt").isNotEmpty())
				.andExpect(jsonPath("$.dueDate").value(LocalDate.now().plusDays(14).toString()))
				.andExpect(jsonPath("$.returnedAt").doesNotExist());
	}

	@Test
	void createLoanWithMissingFieldsReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors", hasSize(2)));
	}

	@Test
	void createLoanWithUnknownReferencesReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loanJson(998L, 999L)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(2)))
				.andExpect(jsonPath("$.errors[0].field").value("bookId"))
				.andExpect(jsonPath("$.errors[1].field").value("memberId"));
	}

	@Test
	void createLoanForBookOnLoanReturnsConflict() throws Exception {
		long bookId = insertBook("吾輩は猫である", "978-4-10-101035-9");
		long memberId = insertMember("山田太郎", "taro@example.com");
		long otherMemberId = insertMember("山田次郎", "jiro@example.com");
		long loanId = createLoan(bookId, memberId);

		mockMvc.perform(post("/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loanJson(bookId, otherMemberId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));

		mockMvc.perform(patch("/loans/" + loanId + "/return"))
				.andExpect(status().isOk());

		// 返却後は別の会員に貸し出せる
		mockMvc.perform(post("/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loanJson(bookId, otherMemberId)))
				.andExpect(status().isCreated());
	}

	@Test
	void returnLoanSetsReturnedAt() throws Exception {
		long bookId = insertBook("吾輩は猫である", "978-4-10-101035-9");
		long memberId = insertMember("山田太郎", "taro@example.com");
		long loanId = createLoan(bookId, memberId);

		mockMvc.perform(patch("/loans/" + loanId + "/return"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.returnedAt").isNotEmpty());

		mockMvc.perform(patch("/loans/" + loanId + "/return"))
				.andExpect(status().isConflict());

		mockMvc.perform(patch("/loans/999/return"))
				.andExpect(status().isNotFound());
	}

	@Test
	void listLoansFiltersByMemberAndBookInLoanedAtDescendingOrder() throws Exception {
		long bookId1 = insertBook("吾輩は猫である", "978-4-10-101035-9");
		long bookId2 = insertBook("坊っちゃん", "978-4-10-101004-5");
		long memberId1 = insertMember("山田太郎", "taro@example.com");
		long memberId2 = insertMember("山田次郎", "jiro@example.com");
		insertLoan(bookId1, memberId1, "2026-06-01 10:00:00", "2026-06-05 10:00:00");
		insertLoan(bookId2, memberId1, "2026-06-10 10:00:00", null);
		insertLoan(bookId1, memberId2, "2026-06-20 10:00:00", null);

		mockMvc.perform(get("/loans"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(3)))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.content[0].book.title").value("吾輩は猫である"))
				.andExpect(jsonPath("$.content[0].member.name").value("山田次郎"));

		mockMvc.perform(get("/loans").param("memberId", String.valueOf(memberId1)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));

		mockMvc.perform(get("/loans")
				.param("memberId", String.valueOf(memberId1))
				.param("bookId", String.valueOf(bookId2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].book.title").value("坊っちゃん"));
	}

	@Test
	void overdueListsOnlyUnreturnedPastDueSortedByDueDate() throws Exception {
		long bookId1 = insertBook("吾輩は猫である", "978-4-10-101035-9");
		long bookId2 = insertBook("坊っちゃん", "978-4-10-101004-5");
		long bookId3 = insertBook("硝子戸の中", "978-4-00-310116-8");
		long bookId4 = insertBook("こころ", "978-4-10-101013-7");
		long memberId = insertMember("山田太郎", "taro@example.com");
		insertLoanWithDueDate(bookId1, memberId, "2026-06-20", null);
		insertLoanWithDueDate(bookId2, memberId, "2026-06-01", null);
		insertLoanWithDueDate(bookId3, memberId, "2099-12-31", null);
		insertLoanWithDueDate(bookId4, memberId, "2026-06-01", "2026-06-10 10:00:00");

		mockMvc.perform(get("/loans/overdue"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].book.title").value("坊っちゃん"))
				.andExpect(jsonPath("$.content[1].book.title").value("吾輩は猫である"));
	}

	@Test
	void loanHistoryStillShowsSoftDeletedBook() throws Exception {
		long bookId = insertBook("吾輩は猫である", "978-4-10-101035-9");
		long memberId = insertMember("山田太郎", "taro@example.com");
		long loanId = createLoan(bookId, memberId);

		mockMvc.perform(patch("/loans/" + loanId + "/return"))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/books/" + bookId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/loans").param("bookId", String.valueOf(bookId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].book.title").value("吾輩は猫である"));
	}

	private long insertBook(String title, String isbn) {
		return jdbcTemplate.queryForObject(
				"INSERT INTO books (title, isbn) VALUES (?, ?) RETURNING id", Long.class, title, isbn);
	}

	private long insertMember(String name, String email) {
		return jdbcTemplate.queryForObject(
				"INSERT INTO members (name, email) VALUES (?, ?) RETURNING id", Long.class, name, email);
	}

	private void insertLoan(long bookId, long memberId, String loanedAt, String returnedAt) {
		jdbcTemplate.update(
				"INSERT INTO loans (book_id, member_id, loaned_at, due_date, returned_at) VALUES (?, ?, ?::timestamp, ?::timestamp + INTERVAL '14 days', ?::timestamp)",
				bookId, memberId, loanedAt, loanedAt, returnedAt);
	}

	private void insertLoanWithDueDate(long bookId, long memberId, String dueDate, String returnedAt) {
		jdbcTemplate.update(
				"INSERT INTO loans (book_id, member_id, due_date, returned_at) VALUES (?, ?, ?::date, ?::timestamp)",
				bookId, memberId, dueDate, returnedAt);
	}

	private String loanJson(Long bookId, Long memberId) throws Exception {
		var body = new java.util.LinkedHashMap<String, Object>();
		body.put("bookId", bookId);
		body.put("memberId", memberId);
		return objectMapper.writeValueAsString(body);
	}

	private long createLoan(long bookId, long memberId) throws Exception {
		MvcResult result = mockMvc.perform(post("/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loanJson(bookId, memberId)))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
	}
}
