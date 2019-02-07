(ns com.theinternate.generators.graph-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.test.check.generators :as gen]
            [com.theinternate.generators.graph :as gen.graph]))

(defn- sample-set
  "Returns the set of values realized from a sampling the generator 1000 times."
  [gen]
  (set (gen/sample gen 10000)))

(deftest gen-directed-acyclic-graph-test
  (is (= #{{:a #{} :b #{} :c #{}}
           {:a #{} :b #{} :c #{:a}}
           {:a #{} :b #{} :c #{:b}}
           {:a #{} :b #{} :c #{:a :b}}
           {:a #{} :b #{:a} :c #{}}
           {:a #{} :b #{:a} :c #{:a}}
           {:a #{} :b #{:a} :c #{:b}}
           {:a #{} :b #{:a} :c #{:a :b}}
           {:a #{} :b #{:c} :c #{}}
           {:a #{} :b #{:c} :c #{:a}}
           {:a #{} :b #{:a :c} :c #{}}
           {:a #{} :b #{:a :c} :c #{:a}}
           {:a #{:b} :b #{} :c #{}}
           {:a #{:b} :b #{} :c #{:a}}
           {:a #{:b} :b #{} :c #{:b}}
           {:a #{:b} :b #{} :c #{:a :b}}
           {:a #{:b} :b #{:c} :c #{}}
           {:a #{:c} :b #{} :c #{}}
           {:a #{:c} :b #{} :c #{:b}}
           {:a #{:c} :b #{:a} :c #{}}
           {:a #{:c} :b #{:c} :c #{}}
           {:a #{:c} :b #{:a :c} :c #{}}
           {:a #{:b :c} :b #{} :c #{}}
           {:a #{:b :c} :b #{} :c #{:b}}
           {:a #{:b :c} :b #{:c} :c #{}}}
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

(defn- mean
  "Returns the mean of the collection of numbers."
  [numbers]
  (/ (reduce + numbers) (count numbers)))

(defn- standard-deviation
  "Returns the standard deviation of the collection of numbers."
  [numbers]
  (Math/sqrt (/ (reduce + (map #(Math/pow (- % (mean numbers)) 2) numbers))
                (count numbers))))

(deftest gen-topological-ordering-distribution-test
  (let [sample (gen/sample (gen.graph/gen-topological-ordering {:a #{:b :c}
                                                                :b #{:d}
                                                                :c #{:d :e}
                                                                :d #{}
                                                                :e #{:f}
                                                                :f #{}})
                           10000)
        counts (map val (frequencies sample))
        deviation (standard-deviation counts)]
    (is (every? #(< (- (mean counts) (* 2 deviation))
                    %
                    (+ (mean counts) (* 2 deviation)))
                counts))))
