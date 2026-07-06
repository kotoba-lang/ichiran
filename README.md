# ichiran

一覧 — a future **tally/rollup-sheet generation actor**: it will read the
itonami append-only ledger and generate/maintain aggregate report workbooks
(`kotoba-lang/sheets` EDN model — workbook/tab/cell/formula/named-range/chart)
as a sealed-intelligence ⊣ independent-governor StateGraph, the same pattern as
[`kekkai`](../kekkai) and [`tayori`](../tayori). See
[ADR-2607062030](../../../90-docs/adr/2607062030-kotoba-lang-ichiran-tally-scaffold.md)
for full context.

> **Status: proposed, scaffold-only.** This repo currently reserves the name,
> registers the dependency shape, and records the future design intent. There
> is **no governor/StateGraph implementation yet** — `src/ichiran/*.cljc` are
> docstring-only stubs. Real implementation is an explicit follow-up, not part
> of this scaffold.

## Future design

From ADR-2607062030 (candidates, not yet implemented):

- **TallyGovernor** HARD invariant candidates — same shape as teian's
  governor: `no-actuation` (the actor proposes report content, it never
  publishes/distributes on its own); `redaction-required` (financial rollups
  are high-sensitivity and must pass a redaction check before any release);
  `tenant-isolation` (a rollup for one tenant must never leak another
  tenant's ledger facts into its cells/formulas).
- **TallyPort** protocol candidates — `fetch-workbook` (read the current
  `sheets.model` workbook for a tally), `propose-revision!` (LLM-drafted cell/
  formula/chart changes as a proposal, not a commit), `publish!` (governor-
  approved only — the only path that makes a revision visible).

## Layout

| Path | Role |
|---|---|
| `src/ichiran/model.cljc` | stub — future: `sheets.model` workbook draft type for itonami-ledger rollups |
| `src/ichiran/governor.cljc` | stub — future: TallyGovernor (`no-actuation` / `redaction-required` / `tenant-isolation`) |
| `src/ichiran/tallyport.cljc` | stub — future: TallyPort protocol (`fetch-workbook` / `propose-revision!` / `publish!`) |
| `test/ichiran/smoke_test.clj` | loads the three stubs and asserts the placeholder contract |

## Run

```bash
clojure -M:test    # smoke test only — no sim yet
clojure -M:lint     # clj-kondo (errors fail)
```

There is no `:run` alias yet — there is no `sim.cljc` to run until the actor
is implemented.
