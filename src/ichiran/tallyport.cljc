(ns ichiran.tallyport
  "Status: proposed — 雛形のみ。governor/operation の実装は follow-up
  （ADR-2607062030）。将来: ichiran.tallyport は TallyPort protocol として
  fetch-workbook（現行 tally の sheets.model workbook を取得）/
  propose-revision!（LLM 起草の cell/formula/chart 変更を proposal として
  返す。commit ではない）/ publish!（governor 承認後のみ有効化する唯一の経路）
  を実装する想定。")

(defn placeholder [] :not-implemented)
