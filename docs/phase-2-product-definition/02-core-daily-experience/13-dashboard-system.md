# Document 13: Dashboard System

## Document Name
Dashboard System

## Purpose

Define the design philosophy for the home/dashboard surface — what content is eligible to appear on it, how that content is prioritized moment to moment, and how the dashboard's composition differs depending on a user's current pillar-mix and life-stage. This document governs the surface a user returns to between anchor moments and proactive interruptions.

## Why It Exists

A dashboard assembled as "a Productivity widget, a Finance widget, and a Health widget side by side" is exactly the three-apps-stapled-together outcome this product exists to avoid, and it leaves no product-level answer for what happens when a user is currently 80% focused on Finance and 5% focused on Health. This document exists so the dashboard is specified as a single adaptive mirror of the user's current state across all three pillars, not a fixed menu of per-pillar modules, and so it stays consistent with the "one assistant, not three apps" principle from the Product (Behavioral) Philosophy Document.

## Approximate Page Count

7-9 pages.

## Sections

1. **Dashboard as Mirror, Not Menu** — the core philosophy distinguishing an adaptive reflection of current state from a static menu of app features.
2. **Content Eligibility Rules** — the categories of information and actions that are allowed to compete for space on the dashboard at all, and which categories are explicitly excluded and reserved for notifications or widgets.
3. **Real-Time Prioritization Logic (Product-Level)** — the product-level rules for how the dashboard re-ranks its content as context changes through the day, referencing the Context Engine — Product Perspective document for the underlying signal detection.
4. **Pillar-Mix Adaptation** — how the dashboard's emphasis shifts for a Finance-heavy user versus a Health-heavy user versus a balanced user.
5. **Life-Stage Adaptation** — how the dashboard changes as a user progresses through the stages defined in the User Journey Map, in step with their current Proactivity Ladder trust level.
6. **Empty/Calm States** — the specified behavior when no pillar has anything urgent to surface, so the dashboard reads as calm and attentive rather than broken or empty.
7. **Relationship to Notifications and Widgets** — the boundary definition establishing the dashboard as the "come back and check" surface, distinct from notifications (push) and widgets (glance), deferring their detailed rules to Documents 14 and 15.
8. **Information Density & Hierarchy Rules** — the behavioral rules for how much can appear at once and in what order of visual/informational priority, referencing the Information Architecture document for structural conventions.
9. **Dashboard Variants Requirement** — the product-behavior rationale for whether the dashboard should have distinct "moods" (e.g., a calm day versus a crunch day), independent of any visual design decisions.

## Deliverables

* Approved Dashboard System document.
* A content-eligibility rule table (what may appear on the dashboard vs. notification vs. widget).
* A set of annotated dashboard states (calm, normal, overloaded) described narratively, one per pillar-mix scenario.

## Dependencies

Requires the **Information Architecture** document (structural conventions for density and hierarchy), the **Context Engine — Product Perspective** document (real-time signal detection), the **Daily Flow** document (Document 10, the dashboard sits inside that rhythm), the **User Personas Document** (pillar-mix variation), and the **Personalization** document.

## Which Teams Use This

Product, Design, Engineering, Data Science/ML.

## Completion Criteria

- [ ] Content eligibility rules have been validated against at least one item from each pillar and at least one cross-pillar item.
- [ ] The empty/calm state has been reviewed and confirmed not to read as broken, blank, or feature-poor.
- [ ] Pillar-mix and life-stage adaptation each have at least one concrete before/after example.
- [ ] The boundary between dashboard, notification, and widget content has been cross-checked against Documents 14 and 15 with no unresolved overlap.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
