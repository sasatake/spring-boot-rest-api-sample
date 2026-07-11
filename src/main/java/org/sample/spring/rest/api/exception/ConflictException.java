package org.sample.spring.rest.api.exception;

public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
