# Document 19: Postmortems

## Document Name
Postmortems

## Purpose
Define the blameless postmortem process that follows every significant incident — how postmortems are written, reviewed, and turned into action items that actually get done, closing the loop that Incident Management (Phase 9 Doc 17) opens.

## Why It Exists
An incident that's resolved but not learned from will recur; without a disciplined postmortem process, the same class of failure repeats because the underlying cause was never systematically addressed — especially costly for a platform whose trust compounds (Phase 5) and whose failures (Phase 1 executive review) have cross-pillar blast radius. Because this product holds irreplaceable financial history, health records, and AI memory at 100M+ user scale, the cost of an unlearned lesson is not just repeated downtime but repeated exposure of a user's most sensitive data and daily life to the same class of failure.

## Approximate Page Count
6-8 pages.

## Sections
1. **Blameless Postmortem Principles** — why blame-free analysis produces better root-cause findings, and the ground rules (no attributing failure to an individual, focus on systems and decisions) that keep a postmortem honest.
2. **Postmortem Document Template** — the required structure: incident timeline, impact assessment, root cause, contributing factors, what went well, and action items.
3. **Mandatory Postmortem Criteria** — which incidents require a postmortem (all SEV1/SEV2 operational incidents per Incident Management, Doc 17; every incident that invoked Disaster Recovery, Phase 4 Doc 35; every incident handed off to Security Incident Response, Phase 6 Doc 21) and the required timeline for drafting one.
4. **Postmortem Review & Publication Process** — the review meeting cadence and required attendees, the internal publication and visibility standard, and the redaction rules applied before a postmortem involving sensitive user data or security details is shared broadly.
5. **Action Item Tracking & Accountability** — how postmortem action items get prioritized against regular roadmap work, assigned an owner and due date, and tracked to actual completion rather than quietly abandoned.
6. **Cross-Incident Trend Analysis** — the recurring review that aggregates postmortems across a quarter to surface systemic patterns — recurring root-cause categories, repeat offending services — invisible within any single postmortem.
7. **Facilitator Training & Postmortem Quality Bar** — the requirement that postmortems be facilitated by someone trained in blameless technique, and the quality rubric used to reject shallow analysis that stops at the first plausible cause instead of the true root cause.
8. **Coordination with Security Incident Postmortems** — how this process interlocks with the postmortem requirement already defined in Security Incident Response (Phase 6 Doc 21) for incidents with a security root cause, so the two processes hand off cleanly rather than producing duplicate or conflicting write-ups.

## Deliverables
* Finalized postmortem document template.
* Mandatory-postmortem trigger criteria mapped to the Incident Management severity rubric.
* Action item tracking system integrated with the engineering roadmap prioritization process.
* Cross-incident trend report, published on a recurring cadence.
* Facilitator training curriculum and postmortem quality rubric.

## Dependencies
Requires Incident Management (Phase 9 Doc 17), Runbooks (Phase 9 Doc 18). Coordinates with Security Incident Response (Phase 6 Doc 21) for security-rooted incidents and Reliability Engineering (Phase 9 Doc 15) for error-budget-impact reporting.

## Teams
SRE, Engineering, VP Engineering/Leadership, AI/ML (AI-specific incidents), Security (handoff incidents)

## Completion Criteria
- [ ] Postmortem template piloted against at least one real or simulated incident.
- [ ] Mandatory-postmortem criteria adopted and cross-checked against the Incident Management severity rubric.
- [ ] Action item tracking system integrated with the engineering roadmap process, with a defined closure SLA.
- [ ] First cross-incident trend review conducted and published.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required).
