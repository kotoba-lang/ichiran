# ichiran — future design (NOT YET IMPLEMENTED)

This document restates the design intent recorded in
[ADR-2607062030](../../../../90-docs/adr/2607062030-kotoba-lang-ichiran-tally-scaffold.md).
Nothing described below exists in `src/` beyond docstring-only stubs — this is
a record of design direction for a follow-up implementation, not a spec of
current behavior.

## Shape

Same sealed-intelligence ⊣ independent-governor StateGraph pattern as
`kekkai`/`tayori`: an LLM proposes, an independent governor commits/holds,
decisions land on an append-only ledger. Here the LLM's job is drafting
**tally/rollup workbooks** (`kotoba-lang/sheets` EDN model —
`workbook`/`tab`/`put-cell`/`put-formula`/`put-cell-style`/`add-named-range`/
`add-chart`) from itonami ledger facts.

## TallyGovernor — HARD invariant candidates

- `no-actuation` — the actor proposes report content; it never
  publishes/distributes a workbook on its own.
- `redaction-required` — financial rollups are high-sensitivity; every
  proposed revision must pass a redaction check before it can be released.
- `tenant-isolation` — a tally for one tenant must never leak another
  tenant's ledger facts into its cells/formulas.

## TallyPort — protocol candidates

- `fetch-workbook` — read the current `sheets.model` workbook for a tally.
- `propose-revision!` — LLM-drafted cell/formula/chart changes as a
  *proposal*, not a commit.
- `publish!` — governor-approved only; the sole path that makes a revision
  visible.

## Non-goals (this scaffold)

- No governor/operation logic, no StateGraph wiring, no store, no phase
  ladder, no advisor. Those are explicit follow-up work per the ADR.
- No wiring from `cloud-itonami` or any other actor into ichiran yet.
