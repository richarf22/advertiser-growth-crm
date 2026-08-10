package org.springframework.samples.petclinic.growth.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.petclinic.growth.api.GrowthDtos.AccountRequest;
import org.springframework.samples.petclinic.growth.model.AdvertiserAccount;
import org.springframework.samples.petclinic.growth.repository.AdvertiserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdvertiserAccountService {
	private final AdvertiserAccountRepository accounts;

	public AdvertiserAccountService(AdvertiserAccountRepository accounts) { this.accounts = accounts; }

	@Transactional
	public AdvertiserAccount create(AccountRequest request) {
		accounts.findByExternalId(request.externalId()).ifPresent(existing -> {
			throw new BusinessConflictException("Advertiser externalId already exists: " + request.externalId());
		});
		AdvertiserAccount account = new AdvertiserAccount();
		apply(account, request);
		return accounts.save(account);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "advertiserAccounts", key = "#id")
	public AdvertiserAccount get(long id) {
		return accounts.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Advertiser account not found: " + id));
	}

	@Transactional
	@CacheEvict(cacheNames = "advertiserAccounts", key = "#id")
	public AdvertiserAccount update(long id, AccountRequest request) {
		AdvertiserAccount account = accounts.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Advertiser account not found: " + id));
		accounts.findByExternalId(request.externalId())
			.filter(other -> !other.getId().equals(id))
			.ifPresent(other -> { throw new BusinessConflictException("Advertiser externalId already exists: " + request.externalId()); });
		apply(account, request);
		return accounts.save(account);
	}

	@Transactional
	@CacheEvict(cacheNames = "advertiserAccounts", key = "#id")
	public void delete(long id) {
		if (!accounts.existsById(id)) throw new ResourceNotFoundException("Advertiser account not found: " + id);
		accounts.deleteById(id);
	}

	private void apply(AdvertiserAccount account, AccountRequest request) {
		account.setExternalId(request.externalId().trim());
		account.setName(request.name().trim());
		account.setRegion(request.region());
		account.setIndustry(request.industry().trim());
	}
}
