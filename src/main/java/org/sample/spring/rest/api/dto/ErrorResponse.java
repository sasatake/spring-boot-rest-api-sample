package org.sample.spring.rest.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String message, List<FieldError> errors) {

	public record FieldError(String field, String message) {
	}
}
