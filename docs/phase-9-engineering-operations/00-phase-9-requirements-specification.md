# Phase 9 — Engineering Operations Requirements

Per `phase9.md`, this document defines every engineering operations document required to build and maintain a world-class software organization. As with prior phases, this is the requirements specification, not the operations handbook itself.

**Relationship to Phase 4:** Phase 4 already produced technical architecture-requirement documents that touch several Phase 9 topics — CI/CD (Doc 30), Feature Flags (Doc 26), Testing Strategy (Doc 52), Code Standards (Doc 53), Release Process (Doc 54), Observability/Logging/Metrics/Tracing (Docs 31–34), Capacity Planning (Doc 38). Those are **architecture** documents (what the system is built to support). Phase 9 is the **practice/process/culture** layer built on top — how engineers actually use that architecture day to day. Every overlapping Phase 9 document below states its boundary against the Phase 4 counterpart explicitly.

---

## Document Set

### Group 1 — Engineering Culture & Standards (`01-engineering-culture-standards/`)

| # | Document | Pages | File |
|---|---|---|---|
| 1 | Engineering Handbook | 8–10 | [`01-engineering-handbook.md`](01-engineering-culture-standards/01-engineering-handbook.md) |
| 2 | Coding Standards (Practice Layer) | 5–7 | [`02-coding-standards-practice-layer.md`](01-engineering-culture-standards/02-coding-standards-practice-layer.md) |
| 3 | Repository Strategy | 6–8 | [`03-repository-strategy.md`](01-engineering-culture-standards/03-repository-strategy.md) |
| 4 | Git Workflow | 5–7 | [`04-git-workflow.md`](01-engineering-culture-standards/04-git-workflow.md) |
| 5 | Documentation Standards | 5–7 | [`05-documentation-standards.md`](01-engineering-culture-standards/05-documentation-standards.md) |

### Group 2 — Delivery Process (`02-delivery-process/`)

| # | Document | Pages | File |
|---|---|---|---|
| 6 | CI/CD (Process Layer) | 5–7 | [`06-ci-cd-process-layer.md`](02-delivery-process/06-ci-cd-process-layer.md) |
| 7 | Release Management | 6–8 | [`07-release-management.md`](02-delivery-process/07-release-management.md) |
| 8 | Feature Flags (Process Layer) | 5–7 | [`08-feature-flags-process-layer.md`](02-delivery-process/08-feature-flags-process-layer.md) |
| 9 | Architecture Review Process | 6–8 | [`09-architecture-review-process.md`](02-delivery-process/09-architecture-review-process.md) |

### Group 3 — Quality & Testing (`03-quality-testing/`)

| # | Document | Pages | File |
|---|---|---|---|
| 10 | Testing Strategy (Process Layer) | 6–8 | [`10-testing-strategy-process-layer.md`](03-quality-testing/10-testing-strategy-process-layer.md) |
| 11 | QA Strategy | 6–8 | [`11-qa-strategy.md`](03-quality-testing/11-qa-strategy.md) |
| 12 | Performance Testing | 6–8 | [`12-performance-testing.md`](03-quality-testing/12-performance-testing.md) |
| 13 | Load Testing | 6–8 | [`13-load-testing.md`](03-quality-testing/13-load-testing.md) |
| 14 | Technical Debt Management | 6–8 | [`14-technical-debt-management.md`](03-quality-testing/14-technical-debt-management.md) |

### Group 4 — Reliability & SRE (`04-reliability-sre/`)

| # | Document | Pages | File |
|---|---|---|---|
| 15 | Reliability Engineering | 7–9 | [`15-reliability-engineering.md`](04-reliability-sre/15-reliability-engineering.md) |
| 16 | SRE Practice | 6–8 | [`16-sre-practice.md`](04-reliability-sre/16-sre-practice.md) |
| 17 | Incident Management | 7–9 | [`17-incident-management.md`](04-reliability-sre/17-incident-management.md) |
| 18 | Runbooks | 6–8 | [`18-runbooks.md`](04-reliability-sre/18-runbooks.md) |
| 19 | Postmortems | 6–8 | [`19-postmortems.md`](04-reliability-sre/19-postmortems.md) |

### Group 5 — Observability Practice (`05-observability-practice/`)

| # | Document | Pages | File |
|---|---|---|---|
| 20 | Monitoring Practice | 6–8 | [`20-monitoring-practice.md`](05-observability-practice/20-monitoring-practice.md) |
| 21 | Logging Practice | 5–7 | [`21-logging-practice.md`](05-observability-practice/21-logging-practice.md) |
| 22 | Metrics Practice | 5–7 | [`22-metrics-practice.md`](05-observability-practice/22-metrics-practice.md) |
| 23 | Tracing Practice | 5–7 | [`23-tracing-practice.md`](05-observability-practice/23-tracing-practice.md) |
| 24 | Alerting | 6–8 | [`24-alerting.md`](05-observability-practice/24-alerting.md) |

### Group 6 — Capacity & DevEx (`06-capacity-devex/`)

| # | Document | Pages | File |
|---|---|---|---|
| 25 | Capacity Planning (Process Layer) | 5–7 | [`25-capacity-planning-process-layer.md`](06-capacity-devex/25-capacity-planning-process-layer.md) |
| 26 | Developer Experience | 6–8 | [`26-developer-experience.md`](06-capacity-devex/26-developer-experience.md) |
| 27 | Internal Tooling | 6–8 | [`27-internal-tooling.md`](06-capacity-devex/27-internal-tooling.md) |

### Group 7 — People & Growth (`07-people-growth/`)

| # | Document | Pages | File |
|---|---|---|---|
| 28 | Engineering Career Ladder | 7–9 | [`28-engineering-career-ladder.md`](07-people-growth/28-engineering-career-ladder.md) |
| 29 | Hiring Standards | 6–8 | [`29-hiring-standards.md`](07-people-growth/29-hiring-standards.md) |
| 30 | Engineering Onboarding | 6–8 | [`30-engineering-onboarding.md`](07-people-growth/30-engineering-onboarding.md) |
| 31 | Knowledge Management | 6–8 | [`31-knowledge-management.md`](07-people-growth/31-knowledge-management.md) |

### Group 8 — On-Call & Productivity Metrics (`08-oncall-and-productivity-metrics/`)

| # | Document | Pages | File |
|---|---|---|---|
| 32 | On-Call Program & Compensation | 6–8 | [`32-oncall-program-compensation.md`](08-oncall-and-productivity-metrics/32-oncall-program-compensation.md) |
| 33 | Engineering Metrics & Productivity Measurement | 6–8 | [`33-engineering-metrics-productivity-measurement.md`](08-oncall-and-productivity-metrics/33-engineering-metrics-productivity-measurement.md) |

---

## Dependency Graph

```
PHASE 1 (Guiding Principles) + PHASE 4 (CI/CD, Testing, Code Standards, Observability, Capacity architecture)
        │
        ▼
01 Engineering Handbook ──(index for all of Phase 9)
        │
        ▼
02 Coding Standards ── 03 Repository Strategy ── 04 Git Workflow ── 05 Documentation Standards
        │
        ▼
06 CI/CD Process ── 07 Release Management ── 08 Feature Flags Process ──> 09 Architecture Review Process
        │
        ▼
10 Testing Strategy Process ──┬──> 11 QA Strategy
                                ├──> 12 Performance Testing
                                ├──> 13 Load Testing
                                └──> 14 Technical Debt Management
        │
        ▼
15 Reliability Engineering ──> 16 SRE Practice ──> 17 Incident Management ──┬──> 18 Runbooks
                                                                             └──> 19 Postmortems
        │
        ▼
20 Monitoring Practice ── 21 Logging Practice ── 22 Metrics Practice ── 23 Tracing Practice ──> 24 Alerting
        │
        ▼
25 Capacity Planning Process ── 26 Developer Experience ── 27 Internal Tooling
        │
        ▼
28 Engineering Career Ladder ──> 29 Hiring Standards
                                   30 Engineering Onboarding
                                   31 Knowledge Management
        │
        ▼
32 On-Call Program & Compensation ── 33 Engineering Metrics & Productivity Measurement
```

## Writing Order

Group 1 (culture/standards, blocking) → Group 2 (delivery process, needs Group 1) → Group 3 (quality, needs Phase 4's Testing Strategy stable) → Group 4 (reliability, needs Group 2's release process) → Group 5 (observability practice, needs Phase 4's observability architecture stable) → Group 6 (capacity/devex) → Group 7 (people, mostly independent — can be parallelized early) → Group 8 (closes the phase, synthesizes Groups 2, 4, 6).

## Critical Path

1. **01 Engineering Handbook** — the index every other Phase 9 document is a chapter of; written first so cross-references resolve correctly.
2. **15 Reliability Engineering → 17 Incident Management** — the backbone of the entire Reliability & SRE group; error budgets (15) determine incident severity classification (17) which determines Runbooks (18) and Postmortems (19).
3. **24 Alerting** — the document where all four Observability Practice documents (20–23) converge into actual on-call action; if alerting practice is wrong, the underlying Phase 4 observability architecture is wasted regardless of quality.
4. **28 Engineering Career Ladder** — blocks Hiring Standards (29), which blocks the entire hiring pipeline as the org scales from founding team to hundreds of engineers.
5. **33 Engineering Metrics & Productivity Measurement** — the capstone synthesis document; explicitly guards against the single biggest risk in this group (metrics misused for individual surveillance/ranking), consistent with the anti-dark-pattern spirit running through every phase of this documentation program.

## Estimated Total Documents

**33 documents** (+ this specification = 34 files in the phase).

## Estimated Total Pages

**~205–240 pages** across 33 documents.

---

## Distinguished Engineer / VP Engineering Self-Review

**Coverage assessment: ~99% complete against `phase9.md`'s 31-item required coverage list, all covered, plus 2 gap-closing additions** (On-Call Program & Compensation, Engineering Metrics & Productivity Measurement — the original list covered SRE/incident practice and individual process areas extensively but had no dedicated on-call sustainability document and no unified cross-cutting productivity-measurement framework tying the process documents together).

What remains open, honestly:

* **Compensation bands / leveling-to-pay mapping** are deliberately excluded from Engineering Career Ladder (28) — leveling criteria are defined, actual compensation is a People/Finance function outside engineering documentation scope.
* **Vendor/tool selection** (which CI provider, which observability platform) is consistently deferred throughout, matching every prior phase's "requirements not final selections" framing — Phase 9 defines the practice around whatever tools Phase 4 eventually selects.
* **Legal/HR policy specifics** (leave policy, harassment reporting) are out of scope — Hiring Standards (29) and On-Call Program (32) touch People/HR-adjacent territory only where it intersects engineering practice, not as a substitute for a full HR handbook.

No other item from `phase9.md`'s required coverage list, and no additional engineering-operations concern the reviewer could identify as necessary for a world-class engineering organization, remains undocumented. Phase 9 is ready to move to detailed drafting, with the Engineering Handbook (01) and Incident Management (17) recommended for earliest review — the former because it indexes everything else, the latter because it's the document most likely to be needed under real pressure before it's been battle-tested.

---

## Program-Wide Note

This completes Phases 1–9 of the documentation program: **~370 documents** across Company Foundation, Product Definition, Product Specifications, Technical Architecture, AI/ML Systems, Security/Privacy/Trust, Design System/UX, Business/GTM, and Engineering Operations. See the root [`docs/README.md`](../README.md) for the full phase index.
