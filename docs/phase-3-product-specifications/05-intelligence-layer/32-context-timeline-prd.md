# Document 32: Context Timeline PRD

## Document Name
Context Timeline PRD

## Purpose
This PRD will define the user-facing, chronological, cross-pillar activity log that shows what the AI observed, inferred, or acted on and when — a visible record of the system's day-to-day behavior. It defines the timeline as a feature (what entries appear, in what order, at what granularity, and with what user controls) and explicitly distinguishes it from AI Memory, which is long-term recall rather than a chronological event log.

## Why It Exists
The Context Engine — Product Perspective (Phase 2) defined what context signals the product recognizes and how they may surface into a suggestion, but it did not define a feature where a user can look back and see the sequence of what happened — every proactive touch, silent observation made visible after the fact, and autonomous action taken on their behalf, in order. Without this PRD, users have no way to audit a week of AI behavior in one place, and "why did it do that" investigations get scattered across pillar-specific screens with no shared chronological view; this is a foundational trust mechanism because a Proactivity Ladder that grants autonomy without a visible record of how that autonomy was used is difficult for a user to hold accountable. This PRD must also draw a hard line against AI Memory so engineering does not build two competing "history" systems.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: the chronological cross-pillar timeline UI, entry types and their source pillar tagging, filtering/search over the timeline, and the visible record of silent-observation and autonomous-action events. Out of scope: long-term durable recall of facts about the user (owned by the AI Memory PRD) and the underlying context signal pipeline/inference architecture (Phase 5).
2. **User Stories** — As a user, I want to scroll back through my week and see everything the AI noticed or did, in order, so I can understand its behavior at a glance; as a user, I want to filter the timeline to just one pillar (e.g., only Finance) when investigating a specific concern; as a user, I want to see an entry for an autonomous action the AI took while I wasn't looking, with a link to undo it if the window is still open; as a user, I want to distinguish between something the AI merely noticed (silent observation, later revealed) and something it actually surfaced to me at the time; as a user reviewing my timeline, I want cross-pillar entries (a Health nudge and a Finance alert on the same day) shown in one unified feed rather than three separate logs.
3. **Functional Requirements** — Define the entry taxonomy (observation-made-visible, suggestion-surfaced, notification-sent, action-taken-autonomously, user-response-recorded), the ordering and grouping rules (chronological, grouped by day, cross-pillar interleaved by default), the filter/search capability by pillar/type/date-range, and the requirement that every autonomous or pre-filled action recorded on the timeline links directly to its undo/override control while that control remains available.
4. **Non-Functional Requirements** — Define the retention window the timeline covers by default and how a user accesses older history, the latency ceiling between an event occurring and its appearing on the timeline, and the privacy constraint that timeline entries never surface a raw context signal the user hasn't consented to viewing (e.g., precise location) even though that signal informed an entry.
5. **UX Requirements** — This feature must conform to the Context Engine — Product Perspective for what context categories may be named in an entry, and to the Automation Philosophy for how each Proactivity Ladder rung's canonical on-screen pattern is represented when replayed on the timeline; feature-specific UX rules must cover how cross-pillar entries are visually tagged by source pillar and how a "silent observation, later revealed" entry is distinguished from an entry that was visible in the moment.
6. **States & Flows** — Enumerate the lifecycle a timeline entry moves through: event occurs → logged → rendered on timeline → [optionally linked to a live undo/override control] → control expires → entry becomes a static historical record, including the flow for a user filtering, searching, and expanding an entry for more detail.
7. **Edge Cases** — Cover an event that belongs to two pillars simultaneously (cross-pillar coordination), a burst of many low-priority entries in a short window that could clutter the feed, an entry whose underlying action was later reversed (how the timeline reflects the reversal), and a user with a long enough history that default retention truncates entries they still want to find.
8. **Failure Scenarios** — Define behavior when the core assumption — that every AI-initiated event is reliably logged — breaks: a logging gap during an offline period, a duplicate entry caused by a retried action, and a case where an entry references a suggestion or memory that has since been deleted or forgotten.
9. **AI Behaviors** — Detail how the timeline itself is read-only with respect to the Proactivity Ladder (it does not grant or revoke autonomy) but must accurately reflect the ladder rung under which each entry occurred; define how the timeline supports the "Autonomy Level Transparency" requirement from the Automation Philosophy by letting a user see, historically, how an autonomy level was exercised over time, and how patterns visible on the timeline (e.g., repeatedly dismissed suggestion type) are expected to already be reflected in the AI's learned behavior rather than requiring separate user action.
10. **Notification Behaviors** — Define that the timeline itself does not generate notifications (it is a pull surface, not a push one), how it relates to the Notification System's log of what was actually sent as a notification versus what was silently logged, and whether a digest/summary notification ("your week in review") may point a user back into the timeline.
11. **Success Criteria** — State the qualitative bar: a user should be able to answer "what has the AI been doing this week" by scrolling one feed, without needing to check three separate pillar screens.
12. **Metrics** — Define quantitative targets such as timeline open rate, average session depth (entries scrolled), filter usage rate, rate of undo actions initiated from a timeline entry, and time-to-find for a user searching for a specific past event.
13. **Open Questions** — Capture unresolved questions such as how long entries remain in default view before archiving, whether silent-observation entries should be revealed on the timeline at all or only in aggregate, and how this feature is reconciled with data-export/deletion requests.

## Deliverables
- Full Context Timeline PRD document following the 13-section structure above.
- Entry-type taxonomy table with pillar tagging rules.
- Timeline entry lifecycle diagram.
- Scope-boundary note distinguishing this feature from AI Memory, for engineering reference.

## Dependencies
Phase 2: Context Engine — Product Perspective, Automation Philosophy, Notification System, User Control Model. Phase 1: Product (Behavioral) Philosophy Document, Guiding Principles Document. Phase 3: AI Memory PRD (for the scope boundary between chronological log and durable recall).

## Teams Using This
Product, Engineering (AI Platform), Design, Trust & Safety, Customer Support, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Entry lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] AI Behaviors section reviewed against the Automation Philosophy's Autonomy Level Transparency requirement.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules for conflicts with other pillars.
- [ ] Scope boundary against the AI Memory PRD confirmed with no functional overlap.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Trust & Safety (required).
