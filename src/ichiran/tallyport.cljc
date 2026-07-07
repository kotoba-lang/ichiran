(ns ichiran.tallyport
  "TallyTarget port — where a tally workbook draft becomes a *real* delivered
  report. A tally-LLM proposal is data (a `:draft` record, content = a
  sheets.model workbook EDN item) until a human approves it; `publish!` is
  called exactly once, after that approval, by `ichiran.operation`'s commit
  step — the actuation (best-effort Transit export + handing the result to
  an injected Distributor). `propose-revision!` is the 'casual commit' analog
  (teian's deckport/koyomi's scheduleport): recording that a draft candidate
  exists, no external effect yet.

  `mock-tallyport` is the default — a deterministic in-memory target so the
  actor is runnable/testable with no network/creds (ADR-2607062030
  Consequences: real Distributor clients need per-provider API tokens, live
  binding is out of scope here). `publish!` exports the workbook via
  `kotoba-lang/sheets`'s `sheets.wire/workbook-envelope` (Kotoba Transit
  JSON) — best-effort: malformed content or any failure to encode simply
  degrades to no envelope bytes rather than failing the whole delivery (a
  wire preview is a nicety, not the actuation itself). The Distributor is a
  plain injected fn — ichiran does not ship a live email/Slack/BI-tool
  client; the default just records what WOULD have been distributed."
  (:require [sheets.wire :as wire]))

(defprotocol TallyTarget
  (fetch-workbook [tt activity] "the artifact's currently delivered workbook content, or nil")
  (propose-revision! [tt activity content] "record `content` as a draft delivery candidate — not yet published. Returns a map (e.g. {:branch ...}) to be merged onto the draft so publish! knows what to deliver.")
  (publish! [tt activity target draft] "export + distribute an already human-approved draft (the store's :draft record) — the actuation"))

(defn- try-export-transit
  "Best-effort Transit envelope export via kotoba-lang/sheets's
  `sheets.wire/workbook-envelope`. Wrapped defensively so a malformed/nil
  workbook, or any encoding failure, degrades to nil instead of throwing —
  publish! must not fail the actuation just because a wire preview couldn't
  be produced."
  [content]
  (when content
    (try (wire/workbook-envelope content)
         (catch #?(:clj Exception :cljs :default) _ nil))))

(defn mock-tallyport
  "A deterministic in-memory TallyTarget: `delivered` is an atom of
  {activity-id -> content} so tests/sim can assert on what WOULD have been
  delivered, without any network call. `distribute-fn` is called exactly
  once per publish! with {:activity :target :content :envelope?} — the
  default is a no-op (a real Distributor — email/Slack/BI-tool/etc — is
  caller-injected, see docs/DESIGN.md; not shipped here)."
  ([] (mock-tallyport (atom {}) (fn [_] nil)))
  ([delivered] (mock-tallyport delivered (fn [_] nil)))
  ([delivered distribute-fn]
   (reify TallyTarget
     (fetch-workbook [_ activity] (get @delivered (:id activity)))
     (propose-revision! [_ activity _content]
       {:branch (str "ichiran/" (:id activity))})
     (publish! [_ activity target draft]
       (let [content  (:content draft)
             envelope (try-export-transit content)]
         (distribute-fn {:activity (:id activity) :target target
                         :content content :envelope? (some? envelope)})
         (swap! delivered assoc (:id activity) content)
         draft)))))
