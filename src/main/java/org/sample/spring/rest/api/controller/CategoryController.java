package org.sample.spring.rest.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.sample.spring.rest.api.dto.CategoryRequest;
import org.sample.spring.rest.api.model.Category;
import org.sample.spring.rest.api.service.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public List<Category> list() {
		return categoryService.list();
	}

	@PostMapping
	public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
		Category category = categoryService.create(request);
		return ResponseEntity.created(URI.create("/categories/" + category.getId())).body(category);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") long id) {
		categoryService.delete(id);
	}
}
