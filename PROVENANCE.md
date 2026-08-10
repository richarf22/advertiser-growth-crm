# Upstream provenance and ownership

- Upstream: <https://github.com/spring-petclinic/spring-petclinic-rest>
- Baseline commit: `698bd832eac0da48e72deb63ce7ab275d98a88fb`
- Baseline date: 2026-08-01
- Upstream license: Apache License 2.0; `LICENSE.txt` is retained.
- Personal extension: the `growth` Java package, CRM schemas and seed data, Redis/Kafka infrastructure,
  Docker packaging, tests, project reports, and the TikTok-targeted resume changes.

The PetClinic endpoints remain available as a reference baseline. The new advertiser account,
opportunity lifecycle, idempotent transition, audit outbox, cache degradation, and event-delivery
flows are implemented as a separate business module rather than presented as upstream work.

