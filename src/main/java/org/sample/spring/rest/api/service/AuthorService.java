package org.sample.spring.rest.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sample.spring.rest.api.dto.AuthorRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.exception.ConflictException;
import org.sample.spring.rest.api.exception.NotFoundException;
import org.sample.spring.rest.api.mapper.AuthorMapper;
import org.sample.spring.rest.api.model.Author;

@Service
public class AuthorService {

	private final AuthorMapper authorMapper;

	public AuthorService(AuthorMapper authorMapper) {
		this.authorMapper = authorMapper;
	}

	@Transactional(readOnly = true)
	public PageResponse<Author> list(int page, int size) {
		long totalElements = authorMapper.countAuthors();
		List<Author> authors = authorMapper.selectAuthors(size, page * size);
		int totalPages = (int) Math.ceilDiv(totalElements, size);
		return new PageResponse<>(authors, totalElements, totalPages);
	}

	@Transactional(readOnly = true)
	public Author get(long id) {
		Author author = authorMapper.selectById(id);
		if (author == null) {
			throw new NotFoundException("author not found: id=" + id);
		}
		return author;
	}

	@Transactional
	public Author create(AuthorRequest request) {
		Author author = new Author();
		author.setName(request.name());
		authorMapper.insert(author);
		return author;
	}

	@Transactional
	public Author update(long id, AuthorRequest request) {
		Author author = get(id);
		author.setName(request.name());
		authorMapper.update(author);
		return author;
	}

	@Transactional
	public void delete(long id) {
		get(id);
		if (authorMapper.countBookReferences(id) > 0) {
			throw new ConflictException("author is referenced by books: id=" + id);
		}
		authorMapper.delete(id);
	}
}
