# Document 28: Search Experience

## Document Name
Search Experience

## Purpose

Define a single, unified search experience across tasks, transactions, meals, and the AI's own memory, so that finding anything the user or the AI has ever recorded feels like querying one assistant rather than searching three separate apps.

## Why It Exists

Tasks, transactions, and meals are naturally different data shapes, and left undefined, each pillar team will default to building its own search box with its own logic, ranking, and empty-state behavior — recreating exactly the silo problem the "one assistant" mission is meant to prevent. Search is also where the "Remember" verb of the Behavioral Loop is most directly tested by the user: if a user has to know which pillar something lives in before they can find it, the AI has failed to remember on the user's behalf. This document exists so search is designed once, centrally, as a cross-pillar capability rather than three parallel and inconsistent ones.

## Approximate Page Count

6-8 pages.

## Sections

1. **The Unified Search Principle** — states that there is exactly one search entry point, one result feed, and one interaction model regardless of which pillar's data is being retrieved.
2. **What Is Searchable** — enumerates the content types in scope: tasks and reminders, transactions and expenses, meals and nutrition logs, goals and habits, and the AI's own memory of past suggestions and conversations.
3. **Result Presentation & Grouping Logic** — how mixed-type results are grouped, labeled, or interleaved on screen, and which relevance signals matter to the user (recency, originating pillar, exact match) described at a product level, not an algorithmic one.
4. **Natural-Language & Conversational Search** — how a user can search using vague, everyday phrasing (e.g., "that restaurant expense last month") instead of exact keywords, and what the AI does when such a query is ambiguous.
5. **Search Entry Points Across the Product** — where search can be invoked from (global search bar, voice, contextual "find similar" actions) and the consistency requirements that hold across every entry point.
6. **Empty States & Zero-Result Handling** — what the user experiences when a search returns nothing, including suggested reformulations or adjacent memory the AI offers instead of a dead end.
7. **Search History & Personalization Boundaries** — whether and how past searches inform future suggestions, and the privacy-conscious limits on what the AI remembers about a user's search behavior.
8. **Cross-Pillar Search Scenarios (Worked Examples)** — at least one worked example per pillar pairing, such as a single query surfacing a Productivity goal, a Health habit entry, and a Finance subscription together.
9. **Out of Scope** — explicitly excludes search indexing architecture, embedding/retrieval model selection, and backend query performance, noting these belong to later engineering and AI-implementation phases.

## Deliverables

* Approved Search Experience document.
* A Unified Search Result Taxonomy defining content-type groupings and display rules.
* A set of at least five worked cross-pillar search scenarios validated against realistic, informal user phrasing.

## Dependencies

Requires the Product Architecture Overview (Document 01) for the Memory Model definition search draws from; the Product Pillars Overview (Document 02) for the content types each pillar owns; and the Voice Interaction Document (27) for consistency where voice is used as a search entry point.

## Which Teams Use This

Product, Design, Engineering (Search/Platform), Data Science/ML, QA, Content/Copy.

## Completion Criteria

- [ ] Every content type listed as "searchable" has a defined display treatment in mixed-result views.
- [ ] At least five cross-pillar search scenarios have been walked through end-to-end, including at least one zero-result case.
- [ ] Natural-language query handling has been validated against ambiguous phrasing for each of the three pillars.
- [ ] Search history/personalization boundaries reviewed against the Product Philosophy Document's trust and consent principles.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
