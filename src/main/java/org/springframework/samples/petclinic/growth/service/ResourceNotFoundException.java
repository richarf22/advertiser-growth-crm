package org.springframework.samples.petclinic.growth.service;

public class ResourceNotFoundException extends RuntimeException {
	public ResourceNotFoundException(String message) { super(message); }
}
