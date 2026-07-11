package org.sample.spring.rest.api.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.sample.spring.rest.api.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidationError(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
				.map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
				.toList();
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
	}

	@ExceptionHandler(InvalidRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidRequest(InvalidRequestException e) {
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), e.getErrors());
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFound(NotFoundException e) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
	}

	@ExceptionHandler(ConflictException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleConflict(ConflictException e) {
		return new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage(), null);
	}

	// アプリ層の事前チェックをすり抜けた DB 制約違反(並行リクエストでの一意制約違反など)を
	// 500 ではなく 409 として返す
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException e) {
		return new ErrorResponse(HttpStatus.CONFLICT.value(), "request conflicts with existing data", null);
	}
}
