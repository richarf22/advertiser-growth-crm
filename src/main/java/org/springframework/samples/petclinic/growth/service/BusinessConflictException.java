package org.springframework.samples.petclinic.growth.service;

public class BusinessConflictException extends RuntimeException {
	public BusinessConflictException(String message) { super(message); }
}
