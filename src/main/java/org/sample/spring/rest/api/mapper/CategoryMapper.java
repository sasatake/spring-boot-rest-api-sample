package org.sample.spring.rest.api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper {

	List<Long> selectExistingIds(@Param("ids") List<Long> ids);
}
