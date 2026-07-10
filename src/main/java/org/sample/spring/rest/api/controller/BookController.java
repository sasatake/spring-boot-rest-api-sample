package org.sample.spring.rest.api.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.sample.spring.rest.api.dto.BookRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.model.Book;
import org.sample.spring.rest.api.service.BookService;

@RestController
@RequestMapping("/books")
public class BookController {

	private static final int MAX_PAGE_SIZE = 100;

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	public PageResponse<Book> list(
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		return bookService.list(title, categoryId, Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
	}

	@GetMapping("/{id}")
	public Book get(@PathVariable("id") long id) {
		return bookService.get(id);
	}

	@PostMapping
	public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
		Book book = bookService.create(request);
		return ResponseEntity.created(URI.create("/books/" + book.getId())).body(book);
	}

	@PutMapping("/{id}")
	public Book update(@PathVariable("id") long id, @Valid @RequestBody BookRequest request) {
		return bookService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") long id) {
		bookService.delete(id);
	}
}
