# Document 05: Search PRD

## Document Name
Search PRD

## Purpose
Define the complete engineering-facing specification for unified search across tasks, transactions, meals, goals/habits, and the AI's own memory: the query-handling pipeline, the ranking and grouping rules for mixed-type results, and the natural-language query support required for a user to search the way they'd ask a person rather than the way they'd query a database.

## Why It Exists
The Search Experience document (Phase 2) establishes that there must be exactly one search entry point and result model across all pillars, but it does not specify the retrieval pipeline, ranking inputs, or the natural-language handling contract engineering needs — without that, "search" risks being implemented as three separate keyword-matching queries stitched together at render time, which fails the moment a user searches with everyday phrasing that spans pillars. This PRD exists because search is the most direct test of whether the AI actually remembers on the user's behalf: if a query about "that restaurant thing last month" cannot resolve to a Finance transaction, a Health meal log, and a related Productivity note in one result set, the product has failed its own Memory Model promise.

## Approximate Page Count
8-10 pages.

## Sections
1. **Feature Scope** — in scope: the unified query pipeline, cross-type ranking and grouping, natural-language/conversational query interpretation, zero-result handling, search entry points and their consistency requirements; out of scope: the underlying retrieval/indexing architecture and embedding or model selection, explicitly deferred to later engineering and AI-implementation phases, and voice-specific input handling, owned by the Voice Assistant PRD (Document 06) which only needs to conform to this PRD's result contract.
2. **User Stories** — e.g., as a user typing "coffee spend this week," I get grouped Finance transactions without needing to specify a category filter; as a user typing a vague phrase like "that place we ate at last month," I get a ranked set of plausible Finance and Health matches with enough context to disambiguate; as a user searching for a half-remembered task, I see it even if my query doesn't share exact keywords with its stored title.
3. **Functional Requirements** — the query-handling pipeline from raw input to structured intent, the content-type coverage list (tasks/reminders, transactions/expenses, meals/nutrition logs, goals/habits, AI memory of past suggestions and conversations), the mixed-result grouping/labeling logic, the natural-language interpretation contract (entity extraction for dates, amounts, merchants, food items, people), and the zero-result reformulation-suggestion behavior.
4. **Non-Functional Requirements** — a search-latency budget for perceived instant response, a relevance-quality bar defined independent of implementation (e.g., minimum acceptable precision for top-3 results on a benchmark query set), and a privacy boundary on what search-history data may be retained and used for future ranking personalization.
5. **UX Requirements** — must conform to the Search Experience document's unified-entry-point principle, result-presentation and grouping rules, and empty-state requirements, and must present voice-originated queries (from the Voice Assistant PRD) through the identical result UI used for typed queries.
6. **States & Flows** — Query-Entered, Interpreting, Results-Grouped, Results-Empty-With-Suggestions, Result-Selected-Deep-Linked, Query-Refined (user edits after seeing results), Search-Abandoned.
7. **Edge Cases** — a query that legitimately matches items across all three pillars simultaneously (e.g., "gym" matching a Health habit, a Finance subscription, and a Productivity calendar block); a query so vague it could reasonably mean several unrelated things; a user searching for something that was deleted or archived; a query containing a typo or informal shorthand common to the user's own past phrasing but not standard usage.
8. **Failure Scenarios** — what happens when the core assumption "natural-language intent can be reliably extracted" breaks: a query is misinterpreted and returns confidently wrong grouped results, or the retrieval layer for one content type is degraded and results silently exclude an entire category without the user knowing coverage was incomplete.
9. **AI Behaviors** — how search leverages the Memory Model and Context Engine to bias ranking toward what the AI predicts is contextually relevant right now (e.g., time-of-month bias toward Finance queries near bill due dates), and how repeated searches for the same unresolved need feed back as a signal the AI should proactively surface next time rather than wait to be searched for again.
10. **Notification Behaviors** — search itself is not a notification surface, but this section specifies whether a repeated zero-result query on a recurring topic is eligible to generate a low-priority proactive suggestion through the Notification System (e.g., offering to set up a recurring reminder for something searched for weekly).
11. **Success Criteria** — a user can find anything they or the AI has ever recorded using their own natural phrasing, without needing to know or specify which pillar it lives in.
12. **Metrics** — query-to-result-click rate, zero-result rate, cross-pillar result frequency (share of queries returning matches from more than one pillar), and reformulation rate after a zero-result response.
13. **Open Questions** — how much search personalization from history is appropriate before it feels like surveillance rather than helpfulness; whether AI memory (past suggestions/conversations) should be searchable by default or opt-in; how zero-result reformulation suggestions should be worded without overreaching into a full conversational agent, a boundary shared with the Voice Assistant PRD.

## Deliverables
* Approved Search PRD.
* Unified query-handling pipeline specification with natural-language entity extraction requirements.
* A mixed-result grouping and ranking rule set.
* At least five worked cross-pillar query scenarios validated against informal phrasing, including one zero-result case.

## Dependencies
Requires the **Search Experience** document (Phase 2, Document 28), the **Product Architecture Overview** document (Phase 2, Document 01) for the Memory Model definition search draws from, the **Memory Model — Behavioral Perspective** document (Phase 2), the **Context Engine — Product Perspective** document (Phase 2), the **Voice Assistant PRD** (Document 06) for the shared result contract, and the **Notification Center PRD** (Document 03) if notification history is included in search scope.

## Teams Using This
Product, Engineering (Search/Platform), Data Science/ML, Design, QA, Content/Copy.

## Completion Criteria
- [ ] Every content type listed in scope has a defined display treatment in mixed-result views.
- [ ] At least five cross-pillar query scenarios walked through end-to-end, including one zero-result case.
- [ ] Natural-language interpretation contract validated against ambiguous phrasing for each of the three pillars.
- [ ] Search-history personalization boundary reviewed against the Product Philosophy Document's trust and consent principles.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Data Science/ML Lead (required).
