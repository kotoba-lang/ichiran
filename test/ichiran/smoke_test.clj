(ns ichiran.smoke-test
  "Scaffold-only smoke test (ADR-2607062030): asserts the namespace stubs load
  without error and their placeholder contract holds. No governor/operation
  behavior exists yet to test."
  (:require [clojure.test :refer [deftest is]]
            [ichiran.model :as model]
            [ichiran.governor :as governor]
            [ichiran.tallyport :as tallyport]))

(deftest stubs-load-and-placeholder-contract-holds
  (is (= :not-implemented (model/placeholder)))
  (is (= :not-implemented (governor/placeholder)))
  (is (= :not-implemented (tallyport/placeholder))))
