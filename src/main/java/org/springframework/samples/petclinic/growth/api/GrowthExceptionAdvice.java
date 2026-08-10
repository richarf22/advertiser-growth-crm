package org.springframework.samples.petclinic.growth.api;

import java.net.URI;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.growth.service.BusinessConflictException;
import org.springframework.samples.petclinic.growth.service.ResourceNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "org.springframework.samples.petclinic.growth")
public class GrowthExceptionAdvice {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler({BusinessConflictException.class, DataIntegrityViolationException.class})
	public ResponseEntity<ProblemDetail> conflict(Exception exception, HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, exception instanceof BusinessConflictException
			? exception.getMessage() : "The request conflicts with persisted data", request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProblemDetail> invalid(ConstraintViolationException exception, HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	private ResponseEntity<ProblemDetail> response(HttpStatus status, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(status.getReasonPhrase());
		problem.setType(URI.create(request.getRequestURI()));
		problem.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(status).body(problem);
	}
}
