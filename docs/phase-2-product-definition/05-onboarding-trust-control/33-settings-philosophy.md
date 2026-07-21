# Document 33: Settings Philosophy

## Document Name
Settings Philosophy

## Purpose

Define how settings are organized across the entire product, and establish the governing philosophy for deciding what ships as a sensible default versus what is exposed as a user-configurable option — avoiding both a product with no meaningful control and a settings menu so large it becomes its own source of overwhelm.

## Why It Exists

An AI that acts proactively across three pillars has an unusually large surface of behavior a user might reasonably want to adjust — notification frequency, automation thresholds, category rules, quiet hours — and without a stated philosophy, two failure modes are equally likely: engineers expose every internal toggle as a setting (recreating the "settings-menu overwhelm" the product's Never Overwhelm principle explicitly rejects), or, in the opposite direction, the team ships only fixed defaults and users feel they have no say over an AI that is acting in increasingly autonomous ways. This document exists to give every team one consistent rule for when something becomes a setting, where it lives, and what its default value is, so settings feel like an extension of the AI's trustworthiness rather than a place users are sent to fight it.

## Approximate Page Count

6-8 pages.

## Sections

1. **Settings Taxonomy** — the top-level categories settings are organized into (e.g., pillar-specific behavior, cross-pillar/global behavior, notifications, automation level, data & privacy) and the rule for which category any new setting belongs to.
2. **Defaults Philosophy** — the criteria for choosing what ships "on" by default versus "off," anchored to the Proactivity Ladder's conservative starting point and the product's mission of approaching zero manual work.
3. **Progressive Configurability** — the distinction between a small set of basic, front-and-center settings and a deeper "advanced" layer, and the rule for what qualifies to live in each.
4. **Per-Pillar Automation Controls** — how the degree of AI initiative for each pillar is exposed as a setting, connecting directly to the override and pause mechanisms defined in the User Control Model.
5. **Search & Discoverability of Settings** — the requirement that any setting a user might reasonably look for can be found without memorizing a menu hierarchy.
6. **Settings Change Feedback** — how the product confirms a settings change has taken effect, so users trust that adjusting a control actually changed AI behavior.
7. **Anti-Patterns to Avoid** — an explicit list of settings-design failure modes this product commits to avoiding (e.g., settings as a dumping ground for unresolved product decisions, duplicate settings across pillars that drift out of sync).
8. **Cross-Device / Cross-Session Consistency** — the experience-level expectation that a setting changed in one place is reflected everywhere the user next encounters the product, stated without prescribing technical implementation.

## Deliverables

* Approved Settings Philosophy document.
* A settings taxonomy map covering every category and where new settings are routed.
* A basic-vs-advanced disclosure ruleset with worked examples.
* An anti-pattern checklist for design and product review of any new setting proposal.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Never Overwhelm constraint and Proactivity Ladder framing; requires the Information Architecture and Navigation Philosophy documents (Documents 03 and 04) for where settings sit within overall product structure; feeds directly into and is used alongside the User Control Model (Document 34), which defines the specific override mechanisms many settings expose.

## Which Teams Use This

Product, Design, Engineering (as downstream consumers), Content/Copy.

## Completion Criteria

- [ ] Every settings category in the taxonomy has a documented rule for what belongs in it.
- [ ] The default value for every settings category is justified against the Proactivity Ladder's conservative starting rung.
- [ ] The basic-vs-advanced disclosure rule has been validated against at least three worked example settings per pillar.
- [ ] The anti-pattern list has been reviewed against at least one existing or proposed setting from each pillar to confirm it catches real cases.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
