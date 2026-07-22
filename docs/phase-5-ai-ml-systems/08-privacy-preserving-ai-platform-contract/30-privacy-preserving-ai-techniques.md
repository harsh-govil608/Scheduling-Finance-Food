# Document 30: Privacy-Preserving AI Techniques

## Document Name
Privacy-Preserving AI Techniques

## Purpose
Define the AI-engineering techniques used to minimize privacy exposure of sensitive user data — SMS content, financial transactions, health photos, location — as it moves through inference and training pipelines: on-device processing preference, data minimization before inference, and anonymization/pseudonymization before training. This document specifies techniques and architectural patterns only; it does not define consent flows, retention policy, regulatory obligations, or data-subject rights, which are owned by the Phase 6 Trust & Safety / Privacy & Data Governance document group.

## Why It Exists
Phase 1's Product Philosophy Document states a Trust & Consent Model at the principle level and explicitly defers detailed policy to Phase 6, but a principle with no engineering technique underneath it is not a control — it is an intention. This product processes categories of data (SMS content, financial transactions, health photos, precise location) that most consumer AI products never touch at all, and it does so specifically to power proactive, autonomous behavior, which means the data must reach inference and training pipelines, not just storage. Without a document defining how privacy is technically preserved at the AI-engineering layer — what gets processed on-device instead of sent to a server, what gets stripped or redacted before a model ever sees it, what gets anonymized before it can influence a trained model's weights — every AI/ML engineer is left to make sensitive-data-handling judgment calls feature by feature, which is exactly the fragmentation Document 01 (AI Platform Overview) and Document 02 (Model Architecture & Selection Strategy) were written to prevent for model selection and must also be prevented for privacy technique.

## Approximate Page Count
8-10 pages

## Sections
1. **Scope & Boundary Against Phase 6 Privacy Policy** — an explicit statement that this document defines AI-engineering techniques (how privacy-preserving processing is implemented in the AI systems layer) and not privacy policy (consent capture, retention schedules, regulatory basis, data-subject access/deletion rights), which are reserved for the Phase 6 Trust & Safety / Privacy & Data Governance document group.
2. **Sensitive Data Category Taxonomy & Handling Tiers** — a tiered classification of AI-consumed data (e.g., Tier 1: SMS content, financial transactions, health photos, precise location; Tier 2: calendar, tasks, general preferences) that determines which minimization, anonymization, or on-device requirement applies, cross-referencing the entity model in Data Architecture & Canonical Data Model (Phase 4, Document 56).
3. **On-Device vs. Cloud Processing Decision Framework** — the criteria (data sensitivity tier, latency budget, model size, device hardware capability) used to decide whether a given inference task runs on-device or is sent to cloud/server-side inference, and the default posture of preferring on-device processing for Tier 1 data whenever feasible.
4. **Data Minimization Before Inference** — techniques for reducing what a model actually sees before a cloud inference call is made: field-level redaction (e.g., stripping account numbers before an LLM parses a transaction description), cropping or masking irrelevant regions of a health photo, truncating SMS content to the minimum span needed for intent classification.
5. **Anonymization & Pseudonymization Before Training** — techniques applied to data used in fine-tuning or training pipelines (feeding Learning Systems, Phase 5) so that training data cannot be traced back to an individual user without a separately held re-identification key, including how pseudonymization keys are managed and rotated.
6. **On-Device Model Constraints & Cloud Fallback Contract** — the capability boundaries of on-device inference given mobile hardware constraints (model size, compute, battery/thermal budget), and the explicit fallback contract for when a Tier 1 task cannot be served on-device, cross-referencing Model Architecture & Selection Strategy (Phase 5, Document 02).
7. **Federated Learning & Differential Privacy Evaluation** — whether and where federated learning or differential-privacy noise injection is used to improve models across the user population without centralizing raw sensitive data, and the evaluation criteria for adopting either technique per task category.
8. **Third-Party Model Provider Data Handling Contract** — how minimization and anonymization techniques apply specifically at the boundary where inference is routed to an external foundation-model provider, including what a "minimized payload" looks like when it must leave the platform's own infrastructure.
9. **Auditability of Privacy Techniques** — the logging and instrumentation requirement so that every inference or training run can be shown, after the fact, to have applied the minimization/anonymization technique its data tier requires — the evidence trail Phase 6 Privacy & Data Governance will rely on for compliance verification.
10. **Technique Validation & Red-Team Testing** — how privacy-preserving techniques are validated before shipping, including adversarial re-identification attempts against anonymized training sets and periodic re-testing as models and data pipelines change.

## Deliverables
- Sensitive data taxonomy with tier-to-technique mapping
- On-device vs. cloud processing decision framework with worked examples per data tier
- Data minimization technique catalog (redaction, cropping, truncation) mapped to data category
- Anonymization/pseudonymization pipeline specification for training data, including key management
- Third-party model provider minimized-payload contract
- Privacy technique audit logging specification
- Red-team validation methodology and cadence

## Dependencies
Requires Phase 1 Product Philosophy Document's Trust & Consent Model (philosophy-level principle this document implements technically); requires Data Architecture & Canonical Data Model (Phase 4, Document 56) for entity sensitivity classification; requires AI Platform Overview (Phase 5, Document 01) and Model Architecture & Selection Strategy (Phase 5, Document 02); informs and is bounded by the future Phase 6 Trust & Safety / Privacy & Data Governance document group, which owns deep privacy policy; informs Learning Systems (Phase 5, document group 04) for anonymized training data intake.

## Teams
AI/ML Engineering, Data Science, Mobile Engineering, Security, Privacy/Legal (advisory), Backend Service Teams (Finance, Health)

## Completion Criteria
- [ ] Every Tier 1 sensitive data category has an explicit on-device-vs-cloud default and a documented minimization technique.
- [ ] Third-party model provider minimized-payload contract reviewed and confirmed sufficient by Security.
- [ ] Anonymization/pseudonymization pipeline validated against at least one red-team re-identification attempt with a documented outcome.
- [ ] Scope boundary against Phase 6 Trust & Safety / Privacy & Data Governance confirmed with no policy content duplicated or contradicted.
- [ ] Signed off by: Head of AI/ML (required), Head of Security (required), Principal Architect (required).
