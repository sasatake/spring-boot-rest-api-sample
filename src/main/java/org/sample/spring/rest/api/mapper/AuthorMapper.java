package org.sample.spring.rest.api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.sample.spring.rest.api.model.Author;

@Mapper
public interface AuthorMapper {

	List<Long> selectExistingIds(@Param("ids") List<Long> ids);

	List<Author> selectAuthors(@Param("limit") int limit, @Param("offset") int offset);

	long countAuthors();

	Author selectById(@Param("id") long id);

	void insert(Author author);

	int update(Author author);

	int delete(@Param("id") long id);

	int countBookReferences(@Param("authorId") long authorId);
}
