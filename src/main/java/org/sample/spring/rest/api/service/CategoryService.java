package org.sample.spring.rest.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sample.spring.rest.api.dto.CategoryRequest;
import org.sample.spring.rest.api.exception.ConflictException;
import org.sample.spring.rest.api.exception.NotFoundException;
import org.sample.spring.rest.api.mapper.CategoryMapper;
import org.sample.spring.rest.api.model.Category;

@Service
public class CategoryService {

	private final CategoryMapper categoryMapper;

	public CategoryService(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	@Transactional(readOnly = true)
	public List<Category> list() {
		return categoryMapper.selectAll();
	}

	@Transactional
	public Category create(CategoryRequest request) {
		Category category = new Category();
		category.setName(request.name());
		categoryMapper.insert(category);
		return category;
	}

	@Transactional
	public void delete(long id) {
		if (categoryMapper.selectById(id) == null) {
			throw new NotFoundException("category not found: id=" + id);
		}
		if (categoryMapper.countBookReferences(id) > 0) {
			throw new ConflictException("category is referenced by books: id=" + id);
		}
		categoryMapper.delete(id);
	}
}
