package org.sample.spring.rest.api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {

	private Long id;
	private Book book;
	private Member member;
	private LocalDateTime loanedAt;
	private LocalDate dueDate;
	private LocalDateTime returnedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public LocalDateTime getLoanedAt() {
		return loanedAt;
	}

	public void setLoanedAt(LocalDateTime loanedAt) {
		this.loanedAt = loanedAt;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDateTime getReturnedAt() {
		return returnedAt;
	}

	public void setReturnedAt(LocalDateTime returnedAt) {
		this.returnedAt = returnedAt;
	}
}
