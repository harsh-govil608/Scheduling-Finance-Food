# Document 33: AI Coach PRD

## Document Name
AI Coach PRD

## Purpose
This PRD will define the proactive coaching feature that synthesizes patterns observed across pillars into encouragement, advice, and gentle course-correction — e.g., surfacing that a user's best-focus days correlate with 7+ hours of sleep. It defines the coaching feature surface (what a coaching insight looks like, when it is allowed to appear, and how its tone is governed) and explicitly does not define the underlying pattern-detection or correlation modeling that produces the insight, which belongs to Phase 5.

## Why It Exists
The Product (Behavioral) Philosophy Document's "Encourage" and "Never Overwhelm" principles are the two verbs most at risk of being violated by a cross-pillar synthesis feature: an insight engine that connects sleep, spend, and focus data has real potential to feel like surveillance or guilt-tripping if tone and framing aren't specified before engineering builds it. This PRD exists so the single riskiest feature in the Intelligence Layer — the one most able to make users feel judged instead of supported — has an explicit, testable contract tying every coaching behavior back to the encouraging-not-guilting stance established in Phase 1, rather than leaving tone to individual engineers' judgment at implementation time.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: the cross-pillar pattern-to-insight surface, insight framing/tone rules, the coaching cadence and dismissal controls. Out of scope: the correlation/pattern-detection modeling that identifies candidate insights (Phase 5), and single-pillar reminders or nudges that don't synthesize across pillars (owned by the respective pillar's PRDs).
2. **User Stories** — As a user, I want the AI to point out a pattern I hadn't noticed myself (e.g., my focus is best on days I sleep 7+ hours) framed as useful information, not as a scolding; as a user having a rough week, I want the Coach to stay quiet or supportive rather than piling on more insights about what's going wrong; as a user, I want to tell the Coach a specific insight type isn't helpful and have it stop suggesting that category; as a user, I want to see the underlying data behind a coaching insight so I can judge whether I trust it; as a user who's made progress on something the Coach previously flagged, I want that progress acknowledged, not silently ignored.
3. **Functional Requirements** — Define the insight candidate lifecycle (a detected pattern must clear a confidence and relevance bar before ever reaching the user), the framing template requirements (observation, not judgment; evidence cited; suggested next step optional and never mandatory), the cadence/frequency ceiling for coaching insights independent of other notification types, and the dismissal/feedback mechanism that lets a user downrank or block an insight category.
4. **Non-Functional Requirements** — Define the minimum evidence threshold (e.g., minimum sample size/time span) required before a cross-pillar correlation is eligible to be shown as an insight, the requirement that insights never be shown during a detected low-mood or high-stress context without extra tone safeguards, and the privacy constraint that a coaching insight never exposes one pillar's raw data to justify a point about another pillar beyond what the user has consented to cross-pillar sharing.
5. **UX Requirements** — This feature must conform to the Product (Behavioral) Philosophy Document's Encourage and Never Overwhelm principles, to Personalization for how tone (encouragement style, formality) adapts per user within bounds, and to the Context Engine — Product Perspective for cross-pillar context visibility rules; feature-specific UX rules must cover how an insight is visually distinguished from a plain suggestion or reminder and how "evidence" is disclosed inline without overwhelming the insight itself.
6. **States & Flows** — Enumerate the lifecycle an insight moves through: pattern-detected (internal) → candidate → eligibility-checked (confidence, tone-context, cadence budget) → surfaced → [acknowledged / dismissed / acted-on / snoozed] → retired or recurring, including the branch where a previously dismissed insight category is later re-earned after enough time or changed user behavior.
7. **Edge Cases** — Cover a pattern that is statistically real but sensitive (e.g., correlating spend with mood), an insight that would be accurate but contradicts what the user explicitly told the AI about themselves, two insights competing for the same coaching slot in the same week, and a user whose data is too sparse for any pattern to clear the evidence threshold.
8. **Failure Scenarios** — Define behavior when the core assumption — that a surfaced pattern is genuinely meaningful and not spurious correlation — breaks: an insight later shown to be a false pattern once more data arrives, a user reporting an insight felt judgmental despite framing safeguards, and repeated insights in the same category that start to feel like nagging.
9. **AI Behaviors** — Detail how the Coach operates within the Proactivity Ladder strictly at the passive-surfacing/active-suggestion rungs by default (coaching insights are observations or suggestions, not autonomous actions), how dismissal and negative feedback demote a specific insight category's eligibility, how sustained positive engagement with a category raises its surfacing priority (not its autonomy — the Coach never acts on a user's behalf), and how the Coach's synthesis draws on AI Memory and cross-pillar context without duplicating either system's role.
10. **Notification Behaviors** — Define whether coaching insights are ever pushed as notifications versus surfaced only passively within the app, how the Coach's cadence ceiling is coordinated with the Notification System's shared interruption budget so it doesn't compete unfairly with reminders or alerts, and the explicit rule against celebratory/encouragement insights being used as red-badge bait.
11. **Success Criteria** — State the qualitative bar: a user should describe a coaching insight as something a supportive friend who pays attention would say, never as something that made them feel watched or judged.
12. **Metrics** — Define quantitative targets such as insight acknowledgment rate, dismissal/mute rate per category, sentiment of user feedback on insights (positive vs. negative reaction rate), rate of insights leading to a self-reported behavior change, and false-pattern retraction rate.
13. **Open Questions** — Capture unresolved questions such as how much explicit evidence to show by default versus on request, whether the Coach should ever proactively check in after a dismissed sensitive insight, and how coaching insight cadence should adapt during detected difficult life periods.

## Deliverables
- Full AI Coach PRD document following the 13-section structure above.
- Insight framing template and tone-safeguard checklist for Content/Copy and Trust & Safety review.
- Insight eligibility/lifecycle state diagram.
- Cadence-and-cross-pillar-coordination reference for the Notification System.

## Dependencies
Phase 2: Context Engine — Product Perspective, Memory Model — Behavioral Perspective, Personalization, Automation Philosophy, Notification System. Phase 1: Product (Behavioral) Philosophy Document, Guiding Principles Document. Phase 3: AI Memory PRD, Context Timeline PRD.

## Teams Using This
Product, AI/ML, Design, Content/Copy, Trust & Safety, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Insight lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder and Automation Philosophy for consistency, confirming the Coach never exceeds active-suggestion autonomy.
- [ ] Every framing template validated against the Product (Behavioral) Philosophy Document's Encourage and Never Overwhelm principles by Content/Copy and Trust & Safety.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules for conflicts with other pillars.
- [ ] Signed off by: Head of Product (required), Head of AI/ML (required), Head of Trust & Safety (required), Head of Content/Copy (required).
