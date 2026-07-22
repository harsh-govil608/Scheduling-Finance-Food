# Document 11: Navigation (Visual)

## Document Name
Navigation (Visual)

## Purpose
Define the actual visual and interaction design of the product's primary navigation surface — the specific pattern (tab bar, drawer, hub-and-spoke, or hybrid), its states, iconography, labeling, and platform variants — that implements the structural rules already committed to in Phase 2's Navigation Philosophy (Document 04). This document decides how "one home, not three apps" looks and behaves on-screen, not what it structurally means.

## Why It Exists
Phase 2's Navigation Philosophy establishes the One-Home Principle, the primary navigation model at an information-architecture level, and the depth-versus-breadth tradeoffs the product commits to — but it deliberately stops short of choosing an actual widget (a bottom tab bar versus a side drawer versus a floating action hub), its visual weight, or how it looks when a cross-pillar moment or a rising Proactivity Ladder trust level changes what's available. Without this document, engineering and design each guess independently at the literal navigation chrome, and the product risks re-introducing the "three apps stapled together" outcome at the pixel level even after the behavioral layer explicitly ruled it out. This document exists to translate the One-Home Principle into one concrete, buildable navigation component that reads as singular and calm at first glance.

## Approximate Page Count
6-8 pages.

## Sections
1. **Navigation Pattern Selection** — the chosen visual pattern (e.g., persistent bottom tab bar with a central AI-entry point) and the rationale for why it best expresses the One-Home Principle from Document 04.
2. **Anatomy & Visual Structure** — the concrete layout of the navigation surface: item count, spacing, icon-plus-label treatment, and how the AI/home entry point is visually distinguished from pillar-specific entries.
3. **State Design** — the visual treatment of active, inactive, badge/attention, and disabled navigation states, cross-referenced against the Notification System's anti-pattern rules to avoid badge-bait styling.
4. **Cross-Pillar Transition Motion** — the visual/motion treatment of moving between pillars or from a cross-pillar suggestion into a pillar-specific view, implementing the Cross-Pillar Navigation Moments described behaviorally in Document 04.
5. **Trust-Level Visual Adaptation** — how the navigation surface's appearance changes, if at all, as a user's Proactivity Ladder rung rises (e.g., a surfaced shortcut becoming visible), implementing Document 04's Navigation and the Proactivity Ladder section.
6. **Responsive & Platform Variants** — how the navigation pattern adapts across phone, tablet, and any secondary surface (e.g., a companion view), and where iOS/Android platform conventions are followed versus overridden.
7. **Iconography & Labeling Rules** — the visual language for navigation icons and labels, ensuring pillar entries read as equal citizens rather than a ranked hierarchy.
8. **Anti-Pattern Visual Checklist** — the literal on-screen expressions of Document 04's anti-patterns (e.g., a visual app-switcher affordance) that this design explicitly forbids.

## Deliverables
* Approved Navigation (Visual) specification.
* Annotated navigation component mockups covering all states (active, inactive, badge, trust-gated).
* A cross-platform navigation variant table (phone/tablet, iOS/Android).
* A visual anti-pattern checklist for design QA.

## Dependencies
Requires Navigation Philosophy (Phase 2, Document 04) for the structural model this document gives visual form to; requires Component Library, Color System, Iconography System, and Motion Principles (Phase 7, Design Foundations & Component System); references Notification System (Phase 2, Document 14) for badge/attention-state anti-patterns.

## Teams
Product, Design, Engineering (iOS), Engineering (Android), QA.

## Completion Criteria
- [ ] The chosen navigation pattern has been validated against the One-Home Principle using the same full cross-pillar user journey referenced in Document 04's completion criteria.
- [ ] Every navigation state (active, inactive, badge, trust-gated, disabled) has an approved visual spec.
- [ ] Cross-pillar transition motion has been reviewed for at least one AI-initiated and one user-initiated transition.
- [ ] The visual anti-pattern checklist has been checked against current design explorations with no violations outstanding.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
