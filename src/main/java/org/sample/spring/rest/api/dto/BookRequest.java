package org.sample.spring.rest.api.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookRequest(
		@NotBlank @Size(max = 255) String title,
		@NotBlank @Pattern(regexp = "^97[89](-?\\d){10}$", message = "must be a valid ISBN-13") String isbn,
		@Min(1000) Integer publishedYear,
		String description,
		@NotEmpty List<Long> authorIds,
		List<Long> categoryIds) {
}
