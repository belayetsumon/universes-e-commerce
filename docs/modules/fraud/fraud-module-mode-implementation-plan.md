# Fraud Module Operational Mode

Fraud administrators can select a global mode at `/admin/fraud/configuration`.

- **ACTIVE**: score and enforce fraud decisions (default when no setting exists).
- **MONITOR_ONLY**: score new assessments, record signals, and persist observations without adding automatic holds, rejects, or cases.
- **INACTIVE**: pause new automatic fraud assessments.

Mode is stored as a protected key `fraud.module.mode` in the existing MySQL `fraud_configurations` table. No schema migration is required. Invalid/inactive configuration is fail-closed.

Existing blocked orders, open fraud cases, previous holds, vendor payout restrictions, and fraud blocklists remain effective independently of mode. OTP, payment provider, authorization, CSRF and independent checkout safeguards are always enforced. Fraud outbox events are still delivered.

Mode updates require fraud admin authority, a reason, CSRF, and an audit entry. Monitoring-only assessments are stamped with `fraud-v1-monitor` and require enforce-mode reassessment before being accepted when ACTIVE resumes.

Local regression recommended: transition among all modes, existing restrictions, monitor-to-active re-evaluation, COD OTP, CSRF/authorization, and valid/invalid configuration.
