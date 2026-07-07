(ns ichiran.tallyport-test
  "slack-tallyport request-building only — no live Slack call anywhere here
  (there is no bot token to call with yet; see README's 'Slack Distributor
  (owner setup required)' section). Every assertion below drives
  slack-tallyport's returned distribute-fn with an injected fake :http-fn
  that just captures the request map, the same fake-http-fn testing shape
  used across this workspace's other real-binding-but-untested clients
  (e.g. tayori.channel.slack)."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [ichiran.tallyport :as tallyport]))

(defn- capturing-http-fn [captured]
  (fn [req]
    (reset! captured req)
    {:status 200 :body "{\"ok\":true}"}))

(deftest slack-tallyport-posts-chat-postMessage-with-bearer-auth
  (testing "the right endpoint, method, bearer-token auth header, and channel"
    (let [captured (atom nil)
          distribute-fn (tallyport/slack-tallyport {:token "xoxb-test-token" :channel "C0123456"
                                                     :http-fn (capturing-http-fn captured)})]
      (distribute-fn {:activity "act-1" :target "finance@example.com"
                      :content {:sheets/title "Q3 Tally Report"} :envelope? true})
      (let [req @captured]
        (is (= "https://slack.com/api/chat.postMessage" (:url req)))
        (is (= :post (:method req)))
        (is (= "Bearer xoxb-test-token" (get-in req [:headers "Authorization"]))
            "the real Slack Web API bearer-token auth header shape, matching
             tayori.channel.slack's already-implemented convention")
        (is (str/starts-with? (get-in req [:headers "Content-Type"]) "application/json"))
        (is (str/includes? (:body req) "\"channel\":\"C0123456\"")
            "posts to the constructor-injected channel, never a hardcoded one")
        (is (str/includes? (:body req) "Q3 Tally Report"))
        (is (str/includes? (:body req) "Transit envelope exported"))))))

(deftest slack-tallyport-notes-missing-envelope-export
  (testing "envelope? false renders a distinct, honest note (no false claim of an attachment)"
    (let [captured (atom nil)
          distribute-fn (tallyport/slack-tallyport {:token "xoxb-test-token" :channel "C0123456"
                                                     :http-fn (capturing-http-fn captured)})]
      (distribute-fn {:activity "act-2" :target "finance@example.com"
                      :content {:sheets/title "Headcount Report"} :envelope? false})
      (is (str/includes? (:body @captured) "no Transit envelope")))))

(deftest slack-tallyport-falls-back-to-activity-id-when-title-missing
  (testing "a malformed/titleless content still produces a well-formed notification"
    (let [captured (atom nil)
          distribute-fn (tallyport/slack-tallyport {:token "xoxb-test-token" :channel "C0123456"
                                                     :http-fn (capturing-http-fn captured)})]
      (distribute-fn {:activity "act-3" :target "finance@example.com" :content nil :envelope? false})
      (is (str/includes? (:body @captured) "act-3")))))

(deftest slack-tallyport-accepts-injected-json-write
  (testing "a caller-injected :json-write (e.g. for a richer payload) is honored instead of the built-in minimal encoder"
    (let [captured (atom nil)
          distribute-fn (tallyport/slack-tallyport {:token "xoxb-test-token" :channel "C0123456"
                                                     :http-fn (capturing-http-fn captured)
                                                     :json-write pr-str})]
      (distribute-fn {:activity "act-4" :target "finance@example.com"
                      :content {:sheets/title "Injected Encoder Report"} :envelope? true})
      (is (str/includes? (:body @captured) ":channel \"C0123456\"")
          "pr-str's EDN-ish shape proves json-write really was swapped out"))))

(deftest slack-tallyport-does-not-mutate-mock-tallyport-default-behavior
  (testing "slack-tallyport is just another distribute-fn — mock-tallyport still records delivered content the same way with or without it"
    (let [delivered (atom {})
          captured (atom nil)
          tt (tallyport/mock-tallyport delivered
                                       (tallyport/slack-tallyport {:token "xoxb-t" :channel "C1"
                                                                   :http-fn (capturing-http-fn captured)}))]
      (tallyport/publish! tt {:id "act-5"} "finance@example.com" {:content {:sheets/title "Delivered Report"}})
      (is (= {:sheets/title "Delivered Report"} (get @delivered "act-5")))
      (is (some? @captured) "the injected distribute-fn was actually called"))))
