package org.sample.spring.rest.api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.sample.spring.rest.api.model.Book;

@Mapper
public interface BookMapper {

	List<Book> selectBooks(@Param("title") String title, @Param("categoryId") Long categoryId,
			@Param("limit") int limit, @Param("offset") int offset);

	long countBooks(@Param("title") String title, @Param("categoryId") Long categoryId);

	Book selectById(@Param("id") long id);

	int countByIsbn(@Param("isbn") String isbn, @Param("excludeId") Long excludeId);

	void insert(Book book);

	int update(Book book);

	int softDelete(@Param("id") long id);

	void insertBookAuthors(@Param("bookId") long bookId, @Param("authorIds") List<Long> authorIds);

	void insertBookCategories(@Param("bookId") long bookId, @Param("categoryIds") List<Long> categoryIds);

	void deleteBookAuthors(@Param("bookId") long bookId);

	void deleteBookCategories(@Param("bookId") long bookId);

	int countActiveLoans(@Param("bookId") long bookId);
}
