package org.sample.spring.rest.api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.sample.spring.rest.api.model.Category;

@Mapper
public interface CategoryMapper {

	List<Long> selectExistingIds(@Param("ids") List<Long> ids);

	List<Category> selectAll();

	Category selectById(@Param("id") long id);

	void insert(Category category);

	int delete(@Param("id") long id);

	int countBookReferences(@Param("categoryId") long categoryId);
}
