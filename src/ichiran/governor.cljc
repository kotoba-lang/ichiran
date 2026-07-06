(ns ichiran.governor
  "Status: proposed — 雛形のみ。governor/operation の実装は follow-up
  （ADR-2607062030）。将来: ichiran.governor は TallyGovernor として
  no-actuation（自動配布しない）/ redaction-required（財務集計は機微度が高く
  redaction 必須）/ tenant-isolation（テナント間のledger事実の漏洩を禁止）の
  HARD 不変条件を実装する想定（teian と同型）。")

(defn placeholder [] :not-implemented)
