(ns dec-test
  "Core dec tests
   We use two general strategies:
   - unit testing
   - property-based testing

   In some cases, test.check generators are used to ensure functions simply do
   not error when run against wildly varying data."
  (:require
    [clojure.test :refer :all]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [clojure.string :as s]
    [environ.core :refer [env]]
    [dec :refer :all]))

;; testing values

(def test-map
  {:people [{:name "foo"
             :addresses [{:city "SEA"}
                         {:city "SFO"}]}
            {:name "bar"
             :addresses [{:city "DEN"}]}]
   :flat "value"
   :top {:next "ok"}})

(def flattened-test-map
  {:people-0-addresses-1-city "SFO"
   :people-1-name "bar"
   :people-0-addresses-0-city "SEA"
   :people-1-addresses-0-city "DEN"
   :people-0-name "foo"
   :flat "value"
   :top-next "ok"})

(def default-path->key (partial #'dec/path->key default-delimiter))

;; generators

(def simple-except-delim-gen (gen/such-that #(not= default-delimiter %) gen/simple-type))
(def not-nil-simple-gen (gen/such-that (complement nil?) simple-except-delim-gen))
(def vector-alpha-numeric-gen (gen/vector gen/string-alphanumeric 1 20))
(def vector-numeric-gen (gen/vector gen/nat 1 10))
(def nested-map-gen
  (gen/hash-map :name gen/string
                :age gen/int
                :nest (gen/hash-map :foo gen/string)
                :nest2 (gen/hash-map
                         :oz (gen/vector not-nil-simple-gen 1 20)
                         :wibble not-nil-simple-gen
                         :wobble not-nil-simple-gen
                         :norf not-nil-simple-gen
                         :dec not-nil-simple-gen)
                :email gen/string
                :admin gen/boolean))

;; tests

(deftest path->key-test

  (testing "Alphanumeric strings"
    (tc/quick-check
      400 (prop/for-all [v vector-alpha-numeric-gen] (default-path->key v))))
  (testing "Numbers"
    (tc/quick-check
      400 (prop/for-all [v vector-numeric-gen] (default-path->key v))))
  (testing "Simple path->key"
    (is (= :foo-bar-baz (default-path->key [:foo :bar :baz]))))

  (testing "path->key with numbers"
    (is (= :foo-0-baz-1 (default-path->key [:foo 0 :baz 1])))))

(deftest flat?-test
  (testing "Already-flat values"
    (is (#'dec/flat? 1))
    (is (#'dec/flat? "flat"))
    (is (#'dec/flat? true))
    (is (#'dec/flat? 2.0)))
  (testing "Already-flat values"
    (is (not (#'dec/flat? [1])))
    (is (not (#'dec/flat? '(1))))
    (is (not (#'dec/flat? {:a 0}))))
  (testing "flat? doesn't error on various values"
    (tc/quick-check
      100
      (prop/for-all [v gen/any]
                    (let [f (#'dec/flat? v)]
                      (or (= false f) f)))))
  (testing "all non-collection values should be considered flat"
    (tc/quick-check
      300 (prop/for-all [v simple-except-delim-gen] (#'dec/flat? v)))))


(deftest find-or-create-test
  (testing "simple path creation"
    (is (= {:foo {}}
           (#'dec/find-or-create {} [:foo :bar]))))
  (testing "simple path creation with numeric index"
    (is (= {:foo [{}]}
           (#'dec/find-or-create {} [:foo 0 :bar]))))
  (testing "simple path creation with numeric non-zero index"
    (is (= {:foo [nil {}]}
           (#'dec/find-or-create {} [:foo 1 :bar]))))
  (testing "simple path creation with nested numeric non-zero index"
    (is (= {:foo [nil [nil [nil]]]}
           (#'dec/find-or-create {} [:foo 1 1 1])))))

(deftest update-in-idx-test
  (testing "simple case"
    (is (=
         {:people [{:addr [nil {:city "sfo"}]}]}
         (#'dec/update-in-idx {} [:people 0 :addr 1 :city] "sfo")))))

(deftest exploded?-test
  (testing "simple true case"
    (is (#'dec/exploded? #"\." "foo"))))

(deftest enflat-test
  (testing "enflat"
    (is (=
         flattened-test-map
         (enflat test-map)))))

(deftest explode-test
  (testing "explode"
    (is (= test-map (explode flattened-test-map)))))

(deftest laws-test
  (testing "identity"
    (tc/quick-check
      300
      (prop/for-all [v nested-map-gen]
                   (= v ((comp explode enflat) v)))))
  ;; function composition is associative but we'll prove it anyway
  (testing "associativity"
    (tc/quick-check
      300
      (prop/for-all
        [v nested-map-gen]
        (let [r1 ((comp (comp enflat explode) enflat) v)
              r2 ((comp enflat (comp explode enflat)) v)]
        (= r1 r2))))))

(deftest non-default-delimiter
  (testing "delimiter = ."
    (is
      (=
       {:dec {:hosts ["a.host.com" "b.host.com"], :level "debug"}}
       (explode {:dec.hosts.0 "a.host.com"
                 :dec.hosts.1 "b.host.com"
                 :dec.level "debug"}
                :delimiter ".")))))
