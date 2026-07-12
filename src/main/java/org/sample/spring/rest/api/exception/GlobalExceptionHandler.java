package org.sample.spring.rest.api.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.sample.spring.rest.api.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException e) {
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "malformed request body", null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		List<ErrorResponse.FieldError> errors = List.of(
				new ErrorResponse.FieldError(e.getName(), "invalid value: " + e.getValue()));
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNoResource(NoResourceFoundException e) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "resource not found: /" + e.getResourcePath(), null);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public ErrorResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "method not allowed: " + e.getMethod(), null);
	}

	// 個別ハンドラのない Spring MVC 例外(415 など)はステータスを保って共通形式に変換し、
	// それ以外は想定外エラーとして 500 を返す。詳細はログにのみ出力し、レスポンスには含めない
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		if (e instanceof org.springframework.web.ErrorResponse springError) {
			int status = springError.getStatusCode().value();
			HttpStatus resolved = HttpStatus.resolve(status);
			String message = resolved != null ? resolved.getReasonPhrase().toLowerCase() : "request error";
			return ResponseEntity.status(status).body(new ErrorResponse(status, message, null));
		}
		logger.error("unexpected error", e);
		return ResponseEntity.internalServerError()
				.body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal server error", null));
	}
}
