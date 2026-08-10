package org.springframework.samples.petclinic.growth.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.OpportunityRequest;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.OpportunityResponse;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.PageResponse;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.TransitionRequest;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.TransitionResponse;
import org.springframework.samples.petclinic.growth.model.Opportunity;
import org.springframework.samples.petclinic.growth.model.OpportunityStage;
import org.springframework.samples.petclinic.growth.service.OpportunityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/growth")
public class OpportunityController {
	private final OpportunityService service;

	public OpportunityController(OpportunityService service) { this.service = service; }

	@PostMapping("/accounts/{accountId}/opportunities")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('GROWTH_MANAGER','ADMIN')")
	public OpportunityResponse create(@PathVariable long accountId,
		@Valid @RequestBody OpportunityRequest request) {
		return OpportunityResponse.from(service.create(accountId, request));
	}

	@GetMapping("/opportunities/{id}")
	@PreAuthorize("hasAnyRole('GROWTH_VIEWER','GROWTH_MANAGER','ADMIN')")
	public OpportunityResponse get(@PathVariable long id) { return OpportunityResponse.from(service.get(id)); }

	@GetMapping("/opportunities")
	@PreAuthorize("hasAnyRole('GROWTH_VIEWER','GROWTH_MANAGER','ADMIN')")
	public PageResponse<OpportunityResponse> search(
		@RequestParam(required = false) Long accountId,
		@RequestParam(required = false) OpportunityStage stage,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(size, 1), 100);
		Page<Opportunity> result = service.search(accountId, stage,
			PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
		List<OpportunityResponse> content = result.getContent().stream().map(OpportunityResponse::from).toList();
		return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	@PostMapping("/opportunities/{id}/transitions")
	@PreAuthorize("hasAnyRole('GROWTH_MANAGER','ADMIN')")
	public TransitionResponse transition(@PathVariable long id,
		@RequestHeader("Idempotency-Key") @NotBlank String requestId,
		@Valid @RequestBody TransitionRequest request) {
		return TransitionResponse.from(service.transition(id, requestId, request));
	}
}
