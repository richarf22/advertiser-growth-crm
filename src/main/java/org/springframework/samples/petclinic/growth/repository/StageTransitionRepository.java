package org.springframework.samples.petclinic.growth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.growth.model.StageTransition;

public interface StageTransitionRepository extends JpaRepository<StageTransition, Long> {
	Optional<StageTransition> findByRequestId(String requestId);
}
