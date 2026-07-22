# Document 43: Email Integration

## Document Name
Email Integration

## Purpose
Define the architecture for connecting to and ingesting user email accounts to extract transaction-relevant content (e-receipts, bill notifications, bank statements) and productivity-relevant content (calendar invites, booking confirmations) that feed the Finance and Productivity services respectively. This document specifies authentication, ingestion, parsing handoff, and data-minimization requirements for the email integration boundary.

## Why It Exists
Not every transaction or scheduling signal arrives via SMS/UPI — a significant share of receipts, subscription renewals, travel bookings, and calendar invites arrive exclusively by email. Without a dedicated email integration architecture, the Finance and Productivity pillars would miss this class of signal entirely, undermining the "proactive" mission. Email is also a materially different integration surface than SMS: it requires OAuth-based account linking rather than OS-level permission grants, involves third-party mailbox providers with their own API rate limits and policies, and carries a broader surface of sensitive personal content that must be excluded from processing. This document exists to define that surface precisely so engineering and privacy teams build a system that extracts only what is needed and nothing more.

## Approximate Page Count
6-8 pages

## Sections
1. **Account Linking & OAuth Architecture** — how users connect mailbox providers (Gmail/Outlook/iCloud-class) via OAuth, scope minimization, and token storage/rotation requirements.
2. **Ingestion Model: Push vs. Poll** — architecture for near-real-time ingestion via provider push/webhook mechanisms versus scheduled polling, and the fallback strategy when push is unavailable.
3. **Content Classification & Pre-Filtering** — how the system identifies transaction-relevant, calendar-relevant, or booking-relevant emails on ingestion before full parsing, to avoid processing unrelated personal correspondence.
4. **Parsing Pipeline Handoff** — the interface contract between filtered email content and the downstream parsing stage (structured extraction of receipts/invites), without detailing the extraction model itself (Phase 5 scope).
5. **Data Minimization & Retention** — what raw email content (subject, body, attachments) is retained, transformed, or discarded after parsing, tied to Phase 1 Trust & Data Stewardship commitments.
6. **Scope Revocation & Re-Authentication Handling** — system behavior when a user revokes mailbox access or an OAuth token expires/is invalidated mid-use.
7. **Multi-Provider Variance** — how the architecture accommodates differing API capabilities, rate limits, and message formats across mailbox providers, coordinated with Phase 2 Localization for region-specific provider prevalence.
8. **Attachment Handling** — architecture for safely retrieving, scanning, and processing PDF/image attachments (e.g., PDF receipts, e-tickets) including malware-scanning requirements.

## Deliverables
- OAuth scope matrix per supported mailbox provider, reviewed against least-privilege principles.
- Ingestion architecture diagram covering push/webhook and polling paths with fallback logic.
- Content classification rule set defining what qualifies an email for downstream processing.
- Data retention table for raw vs. derived email content.
- Failure-mode matrix (token expiry, scope revocation, provider API outage, malformed attachment).

## Dependencies
Requires Event Architecture, Finance Service, and Productivity/Scheduling Service (Phase 4 core documents); informed by Phase 1 Trust & Data Stewardship, Phase 2 Permissions & Consent UX, Phase 2 Localization, and the Phase 3 Expense Capture PRD.

## Teams
Backend Engineering (Finance Service, Productivity Service), Security, Privacy/Legal, Data Platform, Product (Finance and Productivity pillars)

## Completion Criteria
- [ ] OAuth scope matrix reviewed and approved for least-privilege compliance.
- [ ] Data minimization approach reviewed and approved by Privacy/Legal.
- [ ] Ingestion fallback strategy (push unavailable) validated by Backend Engineering.
- [ ] Attachment-scanning requirements reviewed by Security.
- [ ] Signed off by: CTO/VP Engineering (required), Head of Security (required), Head of Privacy (required).
