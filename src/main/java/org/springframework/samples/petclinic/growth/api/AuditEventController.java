package org.springframework.samples.petclinic.growth.api;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.AuditEventResponse;
import org.springframework.samples.petclinic.growth.repository.AuditEventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/growth/audit-events")
public class AuditEventController {
	private final AuditEventRepository events;

	public AuditEventController(AuditEventRepository events) { this.events = events; }

	@GetMapping
	@PreAuthorize("hasAnyRole('GROWTH_VIEWER','GROWTH_MANAGER','ADMIN')")
	public List<AuditEventResponse> list(@RequestParam(defaultValue = "50") int limit) {
		int safeLimit = Math.min(Math.max(limit, 1), 200);
		return events.findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
			.stream().map(AuditEventResponse::from).toList();
	}
}
