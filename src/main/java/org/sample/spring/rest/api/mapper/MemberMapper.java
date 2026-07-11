package org.sample.spring.rest.api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.sample.spring.rest.api.model.Member;

@Mapper
public interface MemberMapper {

	List<Member> selectMembers(@Param("limit") int limit, @Param("offset") int offset);

	long countMembers();

	Member selectById(@Param("id") long id);

	int countByEmail(@Param("email") String email, @Param("excludeId") Long excludeId);

	void insert(Member member);

	int update(Member member);

	int softDelete(@Param("id") long id);

	int countActiveLoans(@Param("memberId") long memberId);
}
