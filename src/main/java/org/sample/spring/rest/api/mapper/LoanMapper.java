package org.sample.spring.rest.api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.sample.spring.rest.api.model.Loan;

@Mapper
public interface LoanMapper {

	List<Loan> selectLoans(@Param("memberId") Long memberId, @Param("bookId") Long bookId,
			@Param("limit") int limit, @Param("offset") int offset);

	long countLoans(@Param("memberId") Long memberId, @Param("bookId") Long bookId);

	List<Loan> selectOverdue(@Param("limit") int limit, @Param("offset") int offset);

	long countOverdue();

	Loan selectById(@Param("id") long id);

	int countActiveByBookId(@Param("bookId") long bookId);

	int countBook(@Param("bookId") long bookId);

	int countMember(@Param("memberId") long memberId);

	void insert(@Param("bookId") long bookId, @Param("memberId") long memberId,
			@Param("loan") Loan loan);

	int markReturned(@Param("id") long id);
}
