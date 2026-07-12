package org.sample.spring.rest.api.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.sample.spring.rest.api.dto.LoanRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.model.Loan;
import org.sample.spring.rest.api.service.LoanService;

@RestController
@RequestMapping("/loans")
public class LoanController {

	private static final int MAX_PAGE_SIZE = 100;

	private final LoanService loanService;

	public LoanController(LoanService loanService) {
		this.loanService = loanService;
	}

	@GetMapping
	public PageResponse<Loan> list(
			@RequestParam(value = "memberId", required = false) Long memberId,
			@RequestParam(value = "bookId", required = false) Long bookId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		return loanService.list(memberId, bookId, Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
	}

	@GetMapping("/overdue")
	public PageResponse<Loan> listOverdue(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		return loanService.listOverdue(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
	}

	@PostMapping
	public ResponseEntity<Loan> create(@Valid @RequestBody LoanRequest request) {
		Loan loan = loanService.create(request);
		return ResponseEntity.created(URI.create("/loans/" + loan.getId())).body(loan);
	}

	@PatchMapping("/{id}/return")
	public Loan returnLoan(@PathVariable("id") long id) {
		return loanService.returnLoan(id);
	}
}
