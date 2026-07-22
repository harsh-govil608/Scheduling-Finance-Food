# Document 42: SMS Integration

## Document Name
SMS Integration

## Purpose
Define the architecture for ingesting SMS messages on-device and routing transaction-relevant content to the Finance Service, without ever transmitting raw, unfiltered SMS content off-device unnecessarily. This document specifies the integration boundary between the mobile OS's SMS APIs and the platform's transaction-capture pipeline, including permissioning, filtering, and failure behavior.

## Why It Exists
SMS/UPI transaction capture is a foundational Finance pillar capability established in the Phase 3 Expense Capture PRD, but that PRD deliberately stopped short of specifying how SMS access is architected at the platform level. Because SMS inboxes contain far more than transaction alerts — personal messages, OTPs, marketing spam — the integration architecture must draw a hard boundary around what leaves the device, when, and in what form. This document exists to give engineering, security, and privacy teams a shared, reviewable specification for that boundary before any implementation begins, and to ensure the design holds up under multi-region carrier variance and platform policy scrutiny (both Apple's and Google's SMS-permission policies are restrictive and change over time).

## Approximate Page Count
6-8 pages

## Sections
1. **On-Device Pre-Filtering** — what parsing happens on-device before anything is sent to the backend, including keyword/sender-pattern filtering to minimize sensitive-data transmission.
2. **Carrier/OS Permission Model** — platform-level SMS access requirements (iOS vs. Android differ significantly here, including Android's `SMS_RETRIEVER` API constraints and iOS's lack of general SMS read access) at an architecture level.
3. **Parsing Pipeline Handoff** — how filtered SMS content flows into the transaction-capture pipeline, defining the interface contract (payload shape, transport, sync vs. async) with the ML parsing stage without detailing the parsing model itself (Phase 5 scope).
4. **Data Minimization & Retention** — what raw SMS content is stored vs. discarded after parsing, and for how long, tied directly to Phase 1's Trust & Data Stewardship commitments.
5. **Failure Handling** — what happens when SMS access is revoked mid-use, when OS-level permission prompts are denied, or when parsing fails downstream; how the user is notified and how manual entry is offered as fallback.
6. **Multi-Carrier / Multi-Region Format Variance** — how the architecture accommodates the Phase 2 Localization document's regional SMS sender-ID and message-format differences across carriers and countries.
7. **Sender Verification & Spoofing Resistance** — how the architecture distinguishes legitimate bank/UPI sender IDs from spoofed or lookalike senders to prevent fraudulent transaction injection.
8. **Offline Queuing & Sync** — how SMS-derived events are queued on-device when connectivity is unavailable and reconciled with the backend once restored.

## Deliverables
- Sequence diagrams for the on-device capture → filter → transmit → parse handoff.
- Permission-request UX/architecture flow for both iOS and Android, referencing Phase 2 Permissions & Consent UX.
- Data classification table specifying what SMS fields are retained, transformed, or discarded, and for how long.
- Failure-mode matrix (permission revoked, parse failure, offline, spoofed sender) with defined system responses.
- Regional sender-ID/format variance appendix covering initial launch markets.

## Dependencies
Requires Event Architecture and Finance Service (Phase 4 core documents); informed by Phase 1 Trust & Data Stewardship, Phase 2 Permissions & Consent UX, Phase 2 Localization, and the Phase 3 Expense Capture PRD.

## Teams
Mobile Engineering, Backend Engineering (Finance Service), Security, Privacy/Legal, Data Platform, Product (Finance pillar)

## Completion Criteria
- [ ] Data minimization approach reviewed and approved by Privacy/Legal.
- [ ] Permission-request flows validated against current iOS and Android platform policies.
- [ ] Failure-mode matrix reviewed with Mobile Engineering and Support.
- [ ] Regional format variance appendix validated against Phase 2 Localization document.
- [ ] Signed off by: CTO/VP Engineering (required), Head of Security (required), Head of Privacy (required).
