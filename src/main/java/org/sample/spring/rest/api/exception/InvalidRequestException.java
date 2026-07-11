package org.sample.spring.rest.api.exception;

import java.util.List;

import org.sample.spring.rest.api.dto.ErrorResponse;

public class InvalidRequestException extends RuntimeException {

	private final List<ErrorResponse.FieldError> errors;

	public InvalidRequestException(List<ErrorResponse.FieldError> errors) {
		super("Validation failed");
		this.errors = errors;
	}

	public List<ErrorResponse.FieldError> getErrors() {
		return errors;
	}
}
