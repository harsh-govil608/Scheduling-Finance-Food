# Document 04: Navigation Philosophy

## Document Name
Navigation Philosophy

## Purpose
Define how users move through the product: the primary navigation model, how the Information Architecture maps onto navigable surfaces, the depth-versus-breadth tradeoffs the product commits to, and the principle that this is "one home, not three apps." This document governs structure of movement, not visual layout or specific screen design.

## Why It Exists

A product built around three pillars is structurally at risk of becoming three tab-bar destinations that merely coexist — the opposite of the "one intelligent assistant" mission. Without an explicit navigation philosophy, individual feature teams will each optimize navigation for their own pillar's convenience, and the result will read to users as three apps sharing a login screen. This document exists to make the "one home" principle a concrete, enforceable navigation rule rather than an aspiration, and to give every future screen-level design document a shared structural skeleton to build within.

## Approximate Page Count
6-8 pages.

## Sections

1. **The One-Home Principle** — the core navigation commitment that the product has a single home surface the AI uses to communicate across all pillars, rather than three pillar-specific home screens.
2. **Primary Navigation Model** — the top-level structure of how a user reaches any part of the product (entry points, persistent navigation elements, how pillars are reached from the home surface).
3. **Mapping Information Architecture to Navigable Surfaces** — how the entities and taxonomy defined in the Information Architecture document translate into actual reachable screens or views.
4. **Depth vs. Breadth Tradeoffs** — the product's stance on how many taps/steps a user should need to reach common actions, and where deeper, less-frequent flows are permitted to be more buried.
5. **Cross-Pillar Navigation Moments** — the navigation-level (not full UX-choreography-level) rules for how a user moves from a Finance-originated suggestion into a Productivity action, for example, referencing the Cross-Pillar Coordination Experience Document for full detail.
6. **Consistency Rules Across Pillars** — the navigation patterns that must be identical regardless of pillar (e.g., how "view history" works everywhere) so the product does not relearn navigation logic per pillar.
7. **Navigation and the Proactivity Ladder** — how navigation adapts, if at all, as a user's trust level rises (e.g., surfaced shortcuts to confirm autonomous actions) without detailing the automation logic itself.
8. **Anti-Patterns** — explicit list of navigation patterns the product forbids (e.g., a separate app-like switcher between pillars) and why each undermines the one-assistant experience.

## Deliverables

* Approved Navigation Philosophy document.
* A primary navigation model diagram (site-map style, product-level, not visual UI).
* An anti-patterns checklist for Design reviews.

## Dependencies

Requires the Product Architecture Overview (Document 01), Product Pillars Overview (Document 02), and Information Architecture (Document 03); references the Cross-Pillar Coordination Experience Document for detailed cross-pillar navigation moments.

## Which Teams Use This

Product, Design, Engineering, QA.

## Completion Criteria

- [ ] The One-Home Principle has been validated against at least one full user journey that touches all three pillars without leaving a single home surface.
- [ ] Every entity type from the Information Architecture document has a defined navigable surface or explicit reason it has none.
- [ ] Depth-vs-breadth tradeoffs have been reviewed against at least one high-frequency action per pillar.
- [ ] The anti-patterns list has been checked against any existing early UI explorations for violations.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
