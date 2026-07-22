# Document 03: Subscription Plans

## Document Name
Subscription Plans

## Purpose
Define the plan tier structure — how many tiers exist, what conceptually differentiates each tier from the next, and how tiers map to user personas and lifecycle stages. This document implements Phase 7's Premium UX (Doc 22) at the commercial layer: it decides *which* plan a premium visual treatment belongs to, not what that treatment looks like.

## Why It Exists
A tiering structure is the primary lever through which the abstract Business Model (Doc 01) and Pricing Strategy (Doc 02) become something a user actually clicks "subscribe" on. Get the tier logic wrong — too many tiers, unclear differentiation, gating the wrong features — and even a sound pricing methodology fails at the point of conversion. This document exists to give Product, Design, and Growth a single source of truth for what each plan *means* conceptually, so that Phase 7's premium visual/UX treatment and Phase 3's feature-level PRDs both have a stable commercial structure to build against, instead of each team inventing its own version of "what premium includes."

## Approximate Page Count
8-10 pages

## Sections
1. **Tier Structure Overview** — the number of tiers (e.g., free/entry, core paid, premium), the naming logic, and the strategic reason for that count (not too many to cause decision paralysis, not so few that upgrade paths disappear).
2. **Tier Differentiation Logic** — the conceptual axis (or axes) along which tiers differ: depth of proactivity, cross-pillar coverage (Productivity/Finance/Health), AI capability level, personalization depth, or usage volume — and why this axis was chosen over alternatives.
3. **Free/Entry Tier Definition** — the purpose of the free tier (acquisition, trust-building, habit formation) and the conceptual boundary of what it must credibly deliver to demonstrate the "proactive AI" value proposition without giving away the full value capture.
4. **Core Paid Tier Definition** — what differentiates the primary paid tier conceptually from free, mapped to the value creation thesis in Doc 01.
5. **Premium Tier Definition** — how the top tier embodies the Premium Experience (Phase 2), Premium Features PRD (Phase 3), and Premium UX (Phase 7, Doc 22) at a commercial level — i.e., which conceptual capability class ("premium" as a business concept) this tier is selling.
6. **Cross-Pillar Bundling Within Tiers** — how Productivity, Finance, and Health capabilities are distributed or bundled across tiers so the product still feels like "one assistant" rather than three products stitched together at different price points.
7. **Upgrade & Downgrade Path Logic** — the conceptual triggers and user journeys that move someone from one tier to the next (and the framework for handling downgrades/cancellation without dark patterns).
8. **Household/Multi-User Plan Considerations** — the framework for whether and how plans extend beyond a single user (family/household plans), given that Finance and Health data are often household-shared.
9. **Plan-to-Persona Mapping** — how each tier maps to the User Personas Document (Phase 1), i.e., which persona each tier is primarily designed to convert and retain.
10. **Tier Evolution & Sunset Policy** — the principles for introducing, modifying, or retiring tiers over time without breaking trust with existing subscribers.

## Deliverables
- A tier structure diagram with tier names, counts, and differentiation axis.
- A capability-to-tier mapping matrix (conceptual, not final feature list) cross-referenced against Phase 3's Premium Features PRD.
- A persona-to-tier mapping table referencing Phase 1 User Personas.
- Upgrade/downgrade journey framework.
- Household/multi-user plan framework (adopted or explicitly deferred, with rationale).

## Dependencies
Requires Business Model (Doc 01), Pricing Strategy (Doc 02), User Personas Document (Phase 1), Premium Experience (Phase 2), Premium Features PRD (Phase 3), Premium UX (Phase 7, Doc 22). Feeds directly into Premium Features — Business Layer (Doc 04) and Monetization Strategy (Doc 05).

## Teams
Product, Design, Growth, Finance, Executive

## Completion Criteria
- [ ] Tier count and differentiation axis justified against both the pricing methodology (Doc 02) and the Premium UX definitions (Phase 7, Doc 22).
- [ ] Each tier explicitly mapped to at least one primary persona from Phase 1.
- [ ] Cross-pillar bundling confirmed to preserve the "one assistant" experience rather than fragmenting Productivity/Finance/Health.
- [ ] Upgrade/downgrade/cancellation flows reviewed against Phase 1 anti-dark-pattern principles.
- [ ] Household/multi-user plan decision documented, even if the decision is to defer.
- [ ] Signed off by: Head of Product (required), Head of Design (required), CEO (required), Head of Growth (required).
