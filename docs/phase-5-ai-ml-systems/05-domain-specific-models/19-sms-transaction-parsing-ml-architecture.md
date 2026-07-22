# Document 19: SMS/Transaction Parsing ML Architecture

## Document Name
SMS/Transaction Parsing ML Architecture

## Purpose
Define the ML architecture that converts the filtered SMS/UPI notification text handed off by Phase 4's SMS Integration into structured transaction records — the classification, extraction, and confidence-scoring model stack behind the Expense Capture PRD's capture-confirm-correct pipeline. This document specifies the model architecture and its interfaces; it does not select a final parsing model or vendor, and it does not redefine the on-device ingestion, permissioning, or transport behavior already owned by Phase 4 Document 42.

## Why It Exists
Expense Capture's entire trust proposition — a ledger accurate enough that a user stops manually checking bank SMS — collapses the moment the parsing layer silently mis-reads an amount or systematically mis-categorizes a merchant, and because this pipeline operates on financial data with effectively no room for a confidently-wrong guess, its confidence behavior cannot be left as an implementation detail decided ad hoc by whichever engineer builds it first. Phase 4's SMS Integration deliberately stopped at "filtered content leaves the device"; this document exists to define, before implementation, exactly how that content becomes a trustworthy structured transaction — including how confidence is scored per field, how an unfamiliar bank template is handled without a wrong guess, and how user corrections make the model measurably better over time — so the Expense Capture PRD's confidence-to-proactivity table has a real model behind it rather than an assumed one.

## Approximate Page Count
8-10 pages

## Sections
1. **Parsing Pipeline Stages** — the stage sequence from filtered message intake through template/sender classification, field extraction, merchant normalization, and category inference, and the interface each stage exposes to the next.
2. **Template & Format Matching Strategy** — the rule-based/template-matching versus learned-extraction split, how the two are combined, and how a message is routed when no known template matches.
3. **Entity Extraction Model Requirements** — architecture-level requirements for extracting amount, merchant, transaction type, date/time, and account/card reference from unstructured message text, including multilingual and regional format coverage tied to Phase 2 Localization.
4. **On-Device vs. Server-Side Inference Split** — the criteria for deciding which extraction stages run on-device versus server-side, driven directly by Phase 4 SMS Integration's data-minimization commitment rather than by convenience or model size alone.
5. **Merchant Normalization & Category Inference** — how raw merchant strings resolve to a normalized merchant entity and a default category, and how per-user category-correction history (surfaced via the Memory & Context and Prediction & Personalization subsystem groups) overrides the population-level default.
6. **Confidence Scoring & Threshold Mapping** — how per-field and overall parse confidence is computed and mapped one-to-one onto the Expense Capture PRD's confidence-to-proactivity decision table (silent auto-log, confirm-required, low-confidence-parked).
7. **Duplicate Detection Model** — how near-simultaneous or retried transaction messages are scored for likely duplication to feed the PRD's duplicate-flagging UX, distinguished from any transport-level deduplication already handled by SMS Integration.
8. **New Format / Cold-Start Handling** — the architecture for detecting a bank or sender template the model has never seen, routing it to low-confidence review rather than a silent guess, and the path by which support for a new format is added and rolled out.
9. **Correction Feedback Loop & Online Learning** — how corrections captured by the Expense Capture PRD's correction flow (recategorization, amount fixes) feed back into per-merchant and per-user model updates, and how that loop is bounded so a single bad correction cannot poison a shared model.
10. **Fraud/Spoofing Signal Consumption** — how sender-legitimacy signals produced by Phase 4 SMS Integration's spoofing-resistance mechanism are consumed as input features to the parsing model, without this document owning spoofing detection itself.

## Deliverables
- Parsing pipeline architecture diagram from filtered-message intake through structured transaction record output.
- Confidence-scoring specification mapped explicitly to the Expense Capture PRD's confidence-to-proactivity decision table.
- Template-matching and cold-start-handling specification, including the new-format rollout path.
- Merchant normalization and category-inference taxonomy, including the personalization override mechanism.
- On-device vs. server-side inference split decision record, justified against SMS Integration's data-minimization requirement.
- Correction feedback loop specification with safeguards against single-user poisoning of shared model behavior.

## Dependencies
Requires AI Platform Overview (Phase 5, Document 01) for the platform's shared subsystem map and non-functional targets; requires SMS Integration (Phase 4, Document 42) as the upstream data contract and data-minimization boundary; requires Data Architecture & Canonical Data Model (Phase 4, Document 56) for the structured transaction record schema; requires the Expense Capture PRD (Phase 3, Document 18) as the product-behavior contract this architecture must satisfy, including its confidence-to-proactivity table, edge cases, and failure scenarios; informed by Phase 2 Localization for regional SMS format variance; draws on the Memory & Context Systems and Prediction & Personalization subsystem groups (Phase 5) for per-user category learning, the Learning Systems group (Phase 5) for the correction feedback loop, and the Privacy-Preserving AI Platform Contract (Phase 5) for on-device processing constraints.

## Teams
AI/ML Engineering, Data Science, Backend Engineering (Finance Service), Mobile Engineering, Security, Privacy/Legal, Product (Finance pillar)

## Completion Criteria
- [ ] Confidence scoring mapped one-to-one against the Expense Capture PRD's confidence-to-proactivity decision table with no unmapped states.
- [ ] On-device vs. server-side inference split reviewed and approved against SMS Integration's data-minimization requirements.
- [ ] Template/cold-start handling validated against at least three unseen bank SMS format scenarios.
- [ ] Correction feedback loop reviewed for consistency with the Learning Systems subsystem group's architecture.
- [ ] Signed off by: Head of AI/ML (required), Principal Architect (required), Head of Privacy (required, for the on-device/server-side split).
