# Document 31: Knowledge Management

## Document Name
Knowledge Management

## Purpose
Define the system by which institutional engineering knowledge — architecture decisions, operational runbooks, tribal knowledge, and the documentation set itself — is captured, kept current, and made discoverable as the company grows from a small founding team to hundreds of engineers. This document is distinct from Documentation Standards (Phase 9 Doc 05), which defines how individual documents are written; this document defines the system engineers use to find, trust, and maintain documents once they exist.

## Why It Exists
At founding-team scale, knowledge lives in people's heads and a quick Slack message or hallway conversation resolves any gap; that model breaks down completely once the org spans hundreds of engineers across 9+ services and cannot rely on everyone having sat in the same room when a decision was made. Without a deliberate knowledge management system, documentation rots (nobody notices a doc is stale until it causes an incident), knowledge fragments across tools nobody can search consistently, and the same question gets re-answered from scratch by every new team, which is precisely the failure mode this document exists to prevent as the documentation set itself grows past what any one engineer can hold in memory.

## Approximate Page Count
6-8 pages

## Sections
1. **Knowledge Taxonomy** — the categories of institutional knowledge covered (architecture decisions, runbooks, postmortems, onboarding material, product/engineering documentation) and where each category is expected to live.
2. **Source-of-Truth Mapping** — for each knowledge category, the single authoritative location and the rule that duplicated copies elsewhere are explicitly non-authoritative.
3. **Discoverability & Search** — the tooling and indexing approach that lets an engineer find the right document without already knowing it exists, including how the 9+ services' individual docs surface in a unified search.
4. **Freshness & Staleness Detection** — the mechanism (automated or process-based) for flagging documents that haven't been reviewed since a relevant system changed, and the review-cadence expectation by document criticality tier.
5. **Ownership & Maintenance Accountability** — how every significant document gets a named owning team, and what happens when ownership needs to transfer as teams reorganize.
6. **Architecture Decision Records (ADRs)** — the practice for capturing why a significant technical decision was made, not just what was decided, so future engineers don't silently re-litigate or accidentally reverse a decision that had a good reason.
7. **Postmortem & Incident Knowledge Capture** — how lessons from incidents (coordinating with the Reliability/SRE practice in Phase 9) get converted into durable, discoverable knowledge rather than a one-time meeting.
8. **Knowledge Retention Through Attrition** — the practice ensuring a departing engineer's undocumented knowledge is captured before they leave, including offboarding knowledge-transfer requirements.
9. **Tooling & Access Model** — the concrete platform(s) used for knowledge storage and search, and how access is scoped (e.g., security-sensitive runbooks) without fragmenting discoverability for everyone else.

## Deliverables
- Knowledge taxonomy with source-of-truth mapping per category
- Staleness-detection mechanism and review-cadence table by document criticality tier
- ADR template and adoption workflow
- Postmortem-to-knowledge-base pipeline definition
- Offboarding knowledge-transfer checklist
- Tooling decision record for the search/storage platform and access-scoping model

## Dependencies
Requires Documentation Standards (Phase 9 Doc 05) for how individual documents are authored, which this document assumes as a precondition. Coordinates with Engineering Onboarding (Phase 9 Doc 30) as the primary consumer of discoverable knowledge for new engineers, and with the Reliability/SRE incident-response practice (Phase 9) for postmortem capture. References the full Phase 4-6 architecture documentation set as the highest-volume knowledge category this system must keep discoverable and current.

## Teams
Platform Engineering, Engineering Leadership, Documentation/Technical Writing (if staffed), Site Reliability Engineering, People/HR (offboarding process)

## Completion Criteria
- [ ] Source-of-truth mapping published with no unresolved category ambiguity across the existing documentation set.
- [ ] Staleness-detection mechanism piloted against at least one real stale document before rollout.
- [ ] ADR template adopted and back-filled for at least the highest-impact prior architecture decisions.
- [ ] Offboarding knowledge-transfer checklist validated against at least one real departure.
- [ ] Signed off by: VP Engineering (required), Head of Platform Engineering (required).
