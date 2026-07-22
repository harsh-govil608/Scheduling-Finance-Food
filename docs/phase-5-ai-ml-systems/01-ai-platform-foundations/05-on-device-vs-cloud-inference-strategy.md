# Document 05: On-Device vs Cloud Inference Strategy

## Document Name
On-Device vs Cloud Inference Strategy

## Purpose
Define the criteria for deciding which AI capabilities run on-device (privacy-sensitive or latency-critical tasks such as SMS pre-filtering, notification triage, and quick classification) versus in the cloud (heavy reasoning, coaching conversations, cross-source prediction), and the architecture supporting both modes coherently. This document defines the decision criteria and hybrid architecture, not a final per-feature placement decision for every current or future feature.

## Why It Exists
The product ingests highly sensitive raw signals — SMS content, location, financial transactions, health metrics — directly on the user's device, and the mission of proactive life management only earns user trust if the Proactivity Ladder's early, low-trust levels (silent observation, passive surfacing) can operate without shipping raw sensitive content off-device before the user has any reason to trust the system with it. At the same time, coaching-quality reasoning and cross-pillar prediction genuinely require cloud-scale models no device can run, so a single all-cloud or all-on-device answer fails either the privacy/latency bar or the reasoning-quality bar; this document exists so that placement is decided by consistent criteria rather than ad hoc per-feature judgment calls that could quietly leak sensitive raw data to the cloud.

## Approximate Page Count
8-10 pages

## Sections
1. **Placement Decision Criteria** — the criteria (data sensitivity, latency requirement, connectivity assumption, model size feasibility on target hardware, battery/thermal cost) used to decide whether a task runs on-device or in the cloud.
2. **On-Device Task Catalog** — the categories of task suited to on-device execution (SMS pre-filtering, keyword/entity extraction, quick classification, wake-word/trigger detection) and the on-device model class constraints (size, quantization, supported hardware tier) each must meet.
3. **Cloud Task Catalog** — the categories of task that require cloud execution (open-ended coaching conversation, cross-source prediction, complex financial reasoning, vision tasks beyond on-device model capacity) and why on-device execution is infeasible for each.
4. **Hybrid & Escalation Patterns** — architectural patterns where an on-device model performs a first pass (e.g., filtering, redaction, summarization) before an escalated cloud call, including what is and is not sent off-device at the escalation boundary.
5. **On-Device Model Lifecycle** — how on-device models are packaged, distributed, updated, and rolled back across device platforms, tying into Model Versioning & Rollout (Document 06) for the on-device-specific update path.
6. **Offline & Degraded-Connectivity Behavior** — what AI functionality remains available when a device has no connectivity, and how on-device results are reconciled with cloud state once connectivity resumes.
7. **Device Capability Tiering** — how the platform handles heterogeneous device hardware (older/low-end devices lacking on-device inference capability) and the fallback behavior for devices below the minimum on-device tier.
8. **Privacy Boundary Enforcement** — the technical enforcement (not just policy) preventing raw sensitive data from leaving the device for tasks classified as on-device-only, and how this is verified/audited.
9. **Cost & Battery Impact Assessment** — the tradeoff framework balancing cloud inference cost against on-device battery/thermal/storage cost when a task could plausibly run in either place.

## Deliverables
- Placement decision criteria and scoring framework
- On-device and cloud task catalogs with worked classification examples
- Hybrid escalation pattern reference architectures with data-boundary diagrams
- On-device model lifecycle and update specification
- Offline/degraded-connectivity behavior specification
- Device capability tiering and minimum-tier fallback policy

## Dependencies
Requires AI Platform Overview (Phase 5, Document 01) and Model Architecture & Selection Strategy (Phase 5, Document 02); requires AI Platform Integration Boundary (Phase 4, Document 57) for data input categories and their sensitivity classification; informs Model Versioning & Rollout (Phase 5, Document 06) for the on-device update path; informs the Privacy-Preserving AI Platform Contract (Phase 5, group 08).

## Teams
AI/ML Engineering, Mobile Engineering, Platform Engineering, Security, Product

## Completion Criteria
- [ ] Placement decision criteria applied to and validated against every AI-touched feature identified in Phase 3 PRDs with no unclassified feature.
- [ ] Privacy boundary enforcement reviewed and confirmed technically sufficient by Security, not policy-only.
- [ ] Offline behavior validated against at least one worked scenario per pillar (Productivity, Finance, Health).
- [ ] Device capability tiering validated against the minimum supported device specification from Phase 2/3 platform requirements.
- [ ] Signed off by: Head of AI/ML (required), Head of Mobile Engineering (required), Head of Security (required).
