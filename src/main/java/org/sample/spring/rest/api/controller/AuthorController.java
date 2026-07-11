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

import org.sample.spring.rest.api.dto.AuthorRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.model.Author;
import org.sample.spring.rest.api.service.AuthorService;

@RestController
@RequestMapping("/authors")
public class AuthorController {

	private static final int MAX_PAGE_SIZE = 100;

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping
	public PageResponse<Author> list(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		return authorService.list(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
	}

	@GetMapping("/{id}")
	public Author get(@PathVariable("id") long id) {
		return authorService.get(id);
	}

	@PostMapping
	public ResponseEntity<Author> create(@Valid @RequestBody AuthorRequest request) {
		Author author = authorService.create(request);
		return ResponseEntity.created(URI.create("/authors/" + author.getId())).body(author);
	}

	@PutMapping("/{id}")
	public Author update(@PathVariable("id") long id, @Valid @RequestBody AuthorRequest request) {
		return authorService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") long id) {
		authorService.delete(id);
	}
}
