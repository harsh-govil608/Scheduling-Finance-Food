# Document 10: Animation

## Document Name
Animation

## Purpose

Define the specific, bespoke animated moments in the product — celebratory micro-animations, loading and progress animations, and AI-thinking/processing indicators — as a curated, meaning-carrying set distinct from the general-purpose transition system defined in the Motion document.

## Why It Exists

In a product built around an AI that proactively manages a user's life, moments like "the assistant is thinking," a habit-streak celebration, or a background sync in progress carry emotional and trust weight far out of proportion to their screen time. Without a specification distinct from generic transitions, these moments risk being skipped entirely — leaving the product feeling unresponsive or robotic — or overused, which turns delight into gimmick and violates the "never overwhelm" principle. This document exists to curate exactly which moments earn a bespoke animation and to define how each one communicates its intent.

## Approximate Page Count

6-8 pages.

## Sections

1. **Animation Philosophy** — the principle that bespoke animation is reserved for moments carrying real meaning (progress, delight, trust, status) rather than applied generally, and its explicit relationship to the Motion document.
2. **AI-Thinking & Processing Indicators** — the animated language for "the assistant is working," covering both proactive background processing and active in-conversation processing.
3. **Loading & Progress Animations** — skeleton states, spinners, and progress indicators, and which is used for which latency band.
4. **Celebratory Micro-Animations** — the animated moments tied to milestones, streaks, and goal completions, and the cross-pillar consistency rules that keep celebration feeling like one voice across Finance, Health, and Productivity.
5. **Error & Attention Animations** — the restrained animated language for surfacing errors or required attention without alarming the user, tying to trust and the "never overwhelm" principle.
6. **Animation Inventory & Approval Gate** — the definitive catalog of every bespoke animated moment permitted in the product, and the review gate required before a new one can be added.
7. **Technical Implementation Constraints** — format and technology guidance (e.g., vector-based versus rendered) and the performance/battery budget assigned to each animation.
8. **Accessibility & Reduced-Motion Fallbacks** — the required static or reduced fallback for every bespoke animation, for users with motion sensitivity or reduced-motion settings enabled.

## Deliverables

* Approved Animation document.
* Animation inventory table (moment, trigger, duration, fallback).
* AI-thinking indicator specification set covering idle, active, and error states.
* Celebration animation set mapped to milestone types across all three pillars.

## Dependencies

Requires the **Motion** document (Phase 7, Document 09), the **Design Language** document (Phase 7), the **Component Library** document (Phase 7, Document 07), the **Gamification Philosophy** document (Phase 2, Document 39), and the **Accessibility** document (Phase 2, Document 36).

## Teams

Design, Design Systems, Motion/Animation Design, Frontend Engineering, Accessibility, Product.

## Completion Criteria

- [ ] The animation inventory contains no undocumented bespoke animation referenced anywhere across the 47 Phase 3 PRDs.
- [ ] Every animation in the inventory has an approved reduced-motion/accessibility fallback.
- [ ] The AI-thinking indicator set has been validated as distinguishable at a glance from generic loading states.
- [ ] The celebratory animation set has been reviewed across Productivity, Finance, and Health so no single pillar reads as more "gamified" than another.
- [ ] Signed off by: Head of Design (required), Design Systems Lead (required).
