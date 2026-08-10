package org.springframework.samples.petclinic.growth.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.growth.model.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
	@Query("select event from AuditEvent event where event.status in :statuses and event.attempts < :maxAttempts order by event.createdAt")
	List<AuditEvent> findReady(@Param("statuses") List<AuditEvent.Status> statuses,
		@Param("maxAttempts") int maxAttempts, Pageable pageable);
}
