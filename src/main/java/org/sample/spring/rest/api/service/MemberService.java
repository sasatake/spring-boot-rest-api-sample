package org.sample.spring.rest.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sample.spring.rest.api.dto.MemberRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.exception.ConflictException;
import org.sample.spring.rest.api.exception.NotFoundException;
import org.sample.spring.rest.api.mapper.MemberMapper;
import org.sample.spring.rest.api.model.Member;

@Service
public class MemberService {

	private final MemberMapper memberMapper;

	public MemberService(MemberMapper memberMapper) {
		this.memberMapper = memberMapper;
	}

	@Transactional(readOnly = true)
	public PageResponse<Member> list(int page, int size) {
		long totalElements = memberMapper.countMembers();
		List<Member> members = memberMapper.selectMembers(size, page * size);
		int totalPages = (int) Math.ceilDiv(totalElements, size);
		return new PageResponse<>(members, totalElements, totalPages);
	}

	@Transactional(readOnly = true)
	public Member get(long id) {
		Member member = memberMapper.selectById(id);
		if (member == null) {
			throw new NotFoundException("member not found: id=" + id);
		}
		return member;
	}

	@Transactional
	public Member create(MemberRequest request) {
		validateEmailUniqueness(request.email(), null);
		Member member = toMember(request);
		memberMapper.insert(member);
		return get(member.getId());
	}

	@Transactional
	public Member update(long id, MemberRequest request) {
		get(id);
		validateEmailUniqueness(request.email(), id);
		Member member = toMember(request);
		member.setId(id);
		memberMapper.update(member);
		return get(id);
	}

	@Transactional
	public void delete(long id) {
		get(id);
		if (memberMapper.countActiveLoans(id) > 0) {
			throw new ConflictException("member has active loans: id=" + id);
		}
		memberMapper.softDelete(id);
	}

	private void validateEmailUniqueness(String email, Long excludeId) {
		if (memberMapper.countByEmail(email, excludeId) > 0) {
			throw new ConflictException("email already exists: " + email);
		}
	}

	private Member toMember(MemberRequest request) {
		Member member = new Member();
		member.setName(request.name());
		member.setEmail(request.email());
		member.setPhone(request.phone());
		return member;
	}
}
