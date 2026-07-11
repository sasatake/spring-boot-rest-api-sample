package org.sample.spring.rest.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(@NotBlank @Size(max = 255) String name) {
}
