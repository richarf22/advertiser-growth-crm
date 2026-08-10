package org.springframework.samples.petclinic.growth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.growth.model.AdvertiserAccount;

public interface AdvertiserAccountRepository extends JpaRepository<AdvertiserAccount, Long> {
	Optional<AdvertiserAccount> findByExternalId(String externalId);
}
