package org.sample.spring.rest.api.service;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sample.spring.rest.api.dto.BookRequest;
import org.sample.spring.rest.api.dto.ErrorResponse;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.exception.ConflictException;
import org.sample.spring.rest.api.exception.InvalidRequestException;
import org.sample.spring.rest.api.exception.NotFoundException;
import org.sample.spring.rest.api.mapper.AuthorMapper;
import org.sample.spring.rest.api.mapper.BookMapper;
import org.sample.spring.rest.api.mapper.CategoryMapper;
import org.sample.spring.rest.api.model.Book;

@Service
public class BookService {

	private final BookMapper bookMapper;
	private final AuthorMapper authorMapper;
	private final CategoryMapper categoryMapper;

	public BookService(BookMapper bookMapper, AuthorMapper authorMapper, CategoryMapper categoryMapper) {
		this.bookMapper = bookMapper;
		this.authorMapper = authorMapper;
		this.categoryMapper = categoryMapper;
	}

	@Transactional(readOnly = true)
	public PageResponse<Book> list(String title, Long categoryId, int page, int size) {
		long totalElements = bookMapper.countBooks(title, categoryId);
		List<Book> books = bookMapper.selectBooks(title, categoryId, size, page * size);
		int totalPages = (int) Math.ceilDiv(totalElements, size);
		return new PageResponse<>(books, totalElements, totalPages);
	}

	@Transactional(readOnly = true)
	public Book get(long id) {
		Book book = bookMapper.selectById(id);
		if (book == null) {
			throw new NotFoundException("book not found: id=" + id);
		}
		return book;
	}

	@Transactional
	public Book create(BookRequest request) {
		validate(request, null);
		Book book = toBook(request);
		bookMapper.insert(book);
		insertRelations(book.getId(), request);
		return get(book.getId());
	}

	@Transactional
	public Book update(long id, BookRequest request) {
		get(id);
		validate(request, id);
		Book book = toBook(request);
		book.setId(id);
		bookMapper.update(book);
		bookMapper.deleteBookAuthors(id);
		bookMapper.deleteBookCategories(id);
		insertRelations(id, request);
		return get(id);
	}

	@Transactional
	public void delete(long id) {
		get(id);
		if (bookMapper.countActiveLoans(id) > 0) {
			throw new ConflictException("book is currently on loan: id=" + id);
		}
		bookMapper.softDelete(id);
	}

	private void validate(BookRequest request, Long excludeId) {
		if (request.publishedYear() != null && request.publishedYear() > Year.now().getValue()) {
			throw new InvalidRequestException(List.of(
					new ErrorResponse.FieldError("publishedYear", "must not be a future year")));
		}
		if (bookMapper.countByIsbn(request.isbn(), excludeId) > 0) {
			throw new ConflictException("isbn already exists: " + request.isbn());
		}
		List<ErrorResponse.FieldError> errors = new ArrayList<>();
		errors.addAll(missingIdErrors("authorIds", request.authorIds(), authorMapper.selectExistingIds(request.authorIds())));
		if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
			errors.addAll(missingIdErrors("categoryIds", request.categoryIds(),
					categoryMapper.selectExistingIds(request.categoryIds())));
		}
		if (!errors.isEmpty()) {
			throw new InvalidRequestException(errors);
		}
	}

	private List<ErrorResponse.FieldError> missingIdErrors(String field, List<Long> requestedIds, List<Long> existingIds) {
		Set<Long> existing = new HashSet<>(existingIds);
		return requestedIds.stream()
				.distinct()
				.filter(id -> !existing.contains(id))
				.map(id -> new ErrorResponse.FieldError(field, "not found: id=" + id))
				.toList();
	}

	private Book toBook(BookRequest request) {
		Book book = new Book();
		book.setTitle(request.title());
		book.setIsbn(request.isbn());
		book.setPublishedYear(request.publishedYear());
		book.setDescription(request.description());
		return book;
	}

	private void insertRelations(long bookId, BookRequest request) {
		bookMapper.insertBookAuthors(bookId, request.authorIds().stream().distinct().toList());
		if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
			bookMapper.insertBookCategories(bookId, request.categoryIds().stream().distinct().toList());
		}
	}
}
