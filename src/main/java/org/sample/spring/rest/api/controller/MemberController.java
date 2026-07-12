package org.sample.spring.rest.api.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.sample.spring.rest.api.dto.MemberRequest;
import org.sample.spring.rest.api.dto.PageResponse;
import org.sample.spring.rest.api.model.Member;
import org.sample.spring.rest.api.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

	private static final int MAX_PAGE_SIZE = 100;

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	public PageResponse<Member> list(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		return memberService.list(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
	}

	@GetMapping("/{id}")
	public Member get(@PathVariable("id") long id) {
		return memberService.get(id);
	}

	@PostMapping
	public ResponseEntity<Member> create(@Valid @RequestBody MemberRequest request) {
		Member member = memberService.create(request);
		return ResponseEntity.created(URI.create("/members/" + member.getId())).body(member);
	}

	@PutMapping("/{id}")
	public Member update(@PathVariable("id") long id, @Valid @RequestBody MemberRequest request) {
		return memberService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") long id) {
		memberService.delete(id);
	}
}
