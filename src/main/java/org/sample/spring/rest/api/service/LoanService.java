package org.sample.spring.rest.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sample.spring.rest.api.dto.ErrorResponse;
import org.sample.spring.rest.api.dto.LoanRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.exception.ConflictException;
import org.sample.spring.rest.api.exception.InvalidRequestException;
import org.sample.spring.rest.api.exception.NotFoundException;
import org.sample.spring.rest.api.mapper.LoanMapper;
import org.sample.spring.rest.api.model.Loan;

@Service
public class LoanService {

	private final LoanMapper loanMapper;

	public LoanService(LoanMapper loanMapper) {
		this.loanMapper = loanMapper;
	}

	@Transactional(readOnly = true)
	public PageResponse<Loan> list(Long memberId, Long bookId, int page, int size) {
		long totalElements = loanMapper.countLoans(memberId, bookId);
		List<Loan> loans = loanMapper.selectLoans(memberId, bookId, size, page * size);
		int totalPages = (int) Math.ceilDiv(totalElements, size);
		return new PageResponse<>(loans, totalElements, totalPages);
	}

	@Transactional(readOnly = true)
	public PageResponse<Loan> listOverdue(int page, int size) {
		long totalElements = loanMapper.countOverdue();
		List<Loan> loans = loanMapper.selectOverdue(size, page * size);
		int totalPages = (int) Math.ceilDiv(totalElements, size);
		return new PageResponse<>(loans, totalElements, totalPages);
	}

	@Transactional
	public Loan create(LoanRequest request) {
		validateReferences(request);
		if (loanMapper.countActiveByBookId(request.bookId()) > 0) {
			throw new ConflictException("book is already on loan: bookId=" + request.bookId());
		}
		Loan loan = new Loan();
		loanMapper.insert(request.bookId(), request.memberId(), loan);
		return get(loan.getId());
	}

	@Transactional
	public Loan returnLoan(long id) {
		Loan loan = get(id);
		if (loan.getReturnedAt() != null) {
			throw new ConflictException("loan is already returned: id=" + id);
		}
		loanMapper.markReturned(id);
		return get(id);
	}

	private Loan get(long id) {
		Loan loan = loanMapper.selectById(id);
		if (loan == null) {
			throw new NotFoundException("loan not found: id=" + id);
		}
		return loan;
	}

	private void validateReferences(LoanRequest request) {
		List<ErrorResponse.FieldError> errors = new ArrayList<>();
		if (loanMapper.countBook(request.bookId()) == 0) {
			errors.add(new ErrorResponse.FieldError("bookId", "not found: id=" + request.bookId()));
		}
		if (loanMapper.countMember(request.memberId()) == 0) {
			errors.add(new ErrorResponse.FieldError("memberId", "not found: id=" + request.memberId()));
		}
		if (!errors.isEmpty()) {
			throw new InvalidRequestException(errors);
		}
	}
}
