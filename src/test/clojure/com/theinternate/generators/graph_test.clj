(ns com.theinternate.generators.graph-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.test.check.generators :as gen]
            [com.theinternate.generators.graph :as gen.graph]))

(defn- sample-set
  "Returns the set of values realized from a sampling the generator 100 times."
  [gen]
  (set (gen/sample gen 100)))

(deftest gen-directed-acyclic-graph-test
  (is (= #{{:a #{} :b #{} :c #{:b}}
           {:a #{} :b #{:a} :c #{}}
           {:a #{} :b #{} :c #{}}
           {:a #{} :b #{:a} :c #{:a}}
           {:a #{} :b #{} :c #{:b :a}}
           {:a #{} :b #{:a} :c #{:b}}
           {:a #{} :b #{:a} :c #{:b :a}}
           {:a #{} :b #{} :c #{:a}}}
         (sample-set (gen.graph/gen-directed-acyclic-graph [:a :b :c])))))

(deftest gen-topological-ordering-test
  (is (= #{[:a :b :c :d :e :f]
           [:a :b :c :e :d :f]
           [:a :b :c :e :f :d]
           [:a :c :b :d :e :f]
           [:a :c :b :e :d :f]
           [:a :c :b :e :f :d]
           [:a :c :e :b :d :f]
           [:a :c :e :b :f :d]
           [:a :c :e :f :b :d]}
         (sample-set (gen.graph/gen-topological-ordering {:a #{:b :c}
                                                          :b #{:d}
                                                          :c #{:d :e}
                                                          :d #{}
                                                          :e #{:f}
                                                          :f #{}})))))

(deftest gen-pruned-directed-acyclic-graph-test
  (is (= #{{}
           {:a #{}}
           {:a #{:b} :b #{}}
           {:a #{:c} :c #{}}
           {:a #{:c} :c #{:e} :e #{}}
           {:a #{:c} :c #{:e} :e #{:f} :f #{}}
           {:a #{:b :c} :b #{} :c #{}}
           {:a #{:b :c} :b #{} :c #{:e} :e #{}}
           {:a #{:b :c} :b #{} :c #{:e} :e #{:f} :f #{}}
           {:a #{:b :c} :b #{:d} :c #{:d} :d #{}}
           {:a #{:b :c} :b #{:d} :c #{:d :e} :d #{} :e #{}}
           {:a #{:b :c} :b #{:d} :c #{:e :d} :d #{} :e #{:f} :f #{}}}
         (sample-set (gen.graph/gen-pruned-directed-acyclic-graph {:a #{:b :c}
                                                                   :b #{:d}
                                                                   :c #{:d :e}
                                                                   :d #{}
                                                                   :e #{:f}
                                                                   :f #{}})))))
