package org.springframework.samples.petclinic.growth.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.AccountRequest;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.AccountResponse;
import org.springframework.samples.petclinic.growth.service.AdvertiserAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/growth/accounts")
public class AdvertiserAccountController {
	private final AdvertiserAccountService service;

	public AdvertiserAccountController(AdvertiserAccountService service) { this.service = service; }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('GROWTH_MANAGER','ADMIN')")
	public AccountResponse create(@Valid @RequestBody AccountRequest request) {
		return AccountResponse.from(service.create(request));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('GROWTH_VIEWER','GROWTH_MANAGER','ADMIN')")
	public AccountResponse get(@PathVariable long id) { return AccountResponse.from(service.get(id)); }

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('GROWTH_MANAGER','ADMIN')")
	public AccountResponse update(@PathVariable long id, @Valid @RequestBody AccountRequest request) {
		return AccountResponse.from(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable long id) { service.delete(id); }
}
