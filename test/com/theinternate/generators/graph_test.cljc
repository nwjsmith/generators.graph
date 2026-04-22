(ns com.theinternate.generators.graph-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.theinternate.generators.graph :as gen.graph]))

(def ^:private gen-vertex-set
  (gen/set gen/keyword))

(defn- roots
  "Returns the vertices of the graph with no incoming edges."
  [graph]
  (let [cited (reduce set/union #{} (vals graph))]
    (set/difference (set (keys graph)) cited)))

(defn- acyclic?
  "Returns true if the directed graph contains no cycles."
  [graph]
  (loop [g graph]
    (if (empty? g)
      true
      (let [rs (roots g)]
        (if (empty? rs)
          false
          (recur (apply dissoc g rs)))))))

(defn- edges
  "Returns a seq of [vertex adjacent] pairs for every edge in the graph."
  [graph]
  (for [[vertex adjacents] graph
        adjacent adjacents]
    [vertex adjacent]))

(def ^:private gen-dag-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-directed-acyclic-graph vertices)]
    {:vertices vertices :graph graph}))

(def ^:private gen-directed-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-directed-graph vertices)]
    {:vertices vertices :graph graph}))

(def ^:private gen-undirected-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-undirected-graph vertices)]
    {:vertices vertices :graph graph}))

(def ^:private gen-topological-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-directed-acyclic-graph vertices)
            ordering (gen.graph/gen-topological-ordering graph)]
    {:vertices vertices :graph graph :ordering ordering}))

(def ^:private gen-subgraph-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-directed-acyclic-graph vertices)
            subgraph (gen.graph/gen-ancestor-closed-subgraph graph)]
    {:graph graph :subgraph subgraph}))

(def ^:private gen-weighted-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-directed-acyclic-graph vertices)
            weighted (gen.graph/gen-weighted graph)]
    {:graph graph :weighted weighted}))

(def ^:private gen-weighted-subgraph-case
  (gen/let [vertices gen-vertex-set
            graph (gen.graph/gen-directed-acyclic-graph vertices)
            weighted (gen.graph/gen-weighted graph)
            subgraph (gen.graph/gen-ancestor-closed-subgraph weighted)]
    {:weighted weighted :subgraph subgraph}))

(defspec gen-directed-acyclic-graph-keys-are-input-vertices
  (prop/for-all [{:keys [vertices graph]} gen-dag-case]
    (is (= (set (keys graph)) vertices))))

(defspec gen-directed-acyclic-graph-adjacents-are-input-vertices
  (prop/for-all [{:keys [vertices graph]} gen-dag-case]
    (is (every? #(every? vertices %) (vals graph)))))

(defspec gen-directed-acyclic-graph-is-acyclic
  (prop/for-all [{:keys [graph]} gen-dag-case]
    (is (acyclic? graph))))

(defspec gen-directed-graph-keys-are-input-vertices
  (prop/for-all [{:keys [vertices graph]} gen-directed-case]
    (is (= (set (keys graph)) vertices))))

(defspec gen-directed-graph-adjacents-are-input-vertices
  (prop/for-all [{:keys [vertices graph]} gen-directed-case]
    (is (every? #(every? vertices %) (vals graph)))))

(defspec gen-directed-graph-has-no-self-loops
  (prop/for-all [{:keys [graph]} gen-directed-case]
    (is (every? (fn [[v adj]] (not (contains? adj v))) graph))))

(defspec gen-undirected-graph-keys-are-input-vertices
  (prop/for-all [{:keys [vertices graph]} gen-undirected-case]
    (is (= (set (keys graph)) vertices))))

(defspec gen-undirected-graph-adjacents-are-input-vertices
  (prop/for-all [{:keys [vertices graph]} gen-undirected-case]
    (is (every? #(every? vertices %) (vals graph)))))

(defspec gen-undirected-graph-has-no-self-loops
  (prop/for-all [{:keys [graph]} gen-undirected-case]
    (is (every? (fn [[v adj]] (not (contains? adj v))) graph))))

(defspec gen-undirected-graph-is-symmetric
  (prop/for-all [{:keys [graph]} gen-undirected-case]
    (is (every? (fn [[u adj]]
                  (every? #(contains? (get graph %) u) adj))
                graph))))

(defspec gen-topological-ordering-is-permutation-of-vertices
  (prop/for-all [{:keys [vertices ordering]} gen-topological-case]
    (is (= (set ordering) vertices))
    (is (= (count ordering) (count vertices)))))

(defspec gen-topological-ordering-respects-edges
  (prop/for-all [{:keys [graph ordering]} gen-topological-case]
    (let [index (zipmap ordering (range))]
      (is (every? (fn [[u v]] (< (index u) (index v))) (edges graph))))))

(defspec gen-ancestor-closed-subgraph-keys-are-subset-of-input
  (prop/for-all [{:keys [graph subgraph]} gen-subgraph-case]
    (is (set/subset? (set (keys subgraph)) (set (keys graph))))))

(defspec gen-ancestor-closed-subgraph-adjacents-are-within-subgraph
  (prop/for-all [{:keys [subgraph]} gen-subgraph-case]
    (let [sub-vertices (set (keys subgraph))]
      (is (every? #(every? sub-vertices %) (vals subgraph))))))

(defspec gen-ancestor-closed-subgraph-is-ancestor-closed
  (prop/for-all [{:keys [graph subgraph]} gen-subgraph-case]
    (let [sub-vertices (set (keys subgraph))]
      (is (every? (fn [[u v]]
                    (or (not (contains? sub-vertices v))
                        (contains? sub-vertices u)))
                  (edges graph))))))

(defspec gen-ancestor-closed-subgraph-preserves-adjacency-type
  (prop/for-all [{:keys [subgraph]} gen-weighted-subgraph-case]
    (is (every? map? (vals subgraph)))))

(defspec gen-ancestor-closed-subgraph-preserves-weights
  (prop/for-all [{:keys [weighted subgraph]} gen-weighted-subgraph-case]
    (is (every? (fn [[v adjacents]]
                  (every? (fn [[n w]] (= w (get-in weighted [v n])))
                          adjacents))
                subgraph))))

(defspec gen-weighted-preserves-vertices
  (prop/for-all [{:keys [graph weighted]} gen-weighted-case]
    (is (= (set (keys weighted)) (set (keys graph))))))

(defspec gen-weighted-preserves-edges
  (prop/for-all [{:keys [graph weighted]} gen-weighted-case]
    (is (every? (fn [[v adj]]
                  (= (set (keys (get weighted v))) adj))
                graph))))

(defspec gen-weighted-weights-are-non-negative-integers
  (prop/for-all [{:keys [weighted]} gen-weighted-case]
    (is (every? #(and (integer? %) (not (neg? %)))
                (mapcat vals (vals weighted))))))

(def ^:private max-coverage-vertices
  ;; Coverage-by-enumeration requires a small cap: the theoretical output
  ;; count grows as 2^(n²-n) for directed graphs, so even at n=4 we would
  ;; need to sample ~53k graphs per iteration to reliably cover all 4096
  ;; outputs. At n=3 the max is 64 outputs.
  3)

(def ^:private coverage-sample-count
  ;; 30× the worst-case theoretical output count gives P(miss) ≈ k·e⁻³⁰ per
  ;; iteration — negligible even compounded over many defspec iterations.
  (* 30 (long (Math/pow 2 (* max-coverage-vertices
                             (dec max-coverage-vertices))))))

(def ^:private gen-coverage-vertex-set
  (gen/set gen/keyword {:max-elements max-coverage-vertices}))

(def ^:private gen-coverage-dag
  (gen/let [vertices gen-coverage-vertex-set
            graph (gen.graph/gen-directed-acyclic-graph vertices)]
    graph))

(defn- bit-subsets
  "Returns a seq of all subsets of the given collection."
  [coll]
  (let [items (vec coll)
        n (count items)]
    (for [mask (range (bit-shift-left 1 n))]
      (into #{} (for [i (range n) :when (bit-test mask i)] (nth items i))))))

(defn- induced-subgraph
  "Returns the subgraph of graph induced by the given vertex set."
  [graph vertex-set]
  (into {}
        (for [[v adj] graph :when (contains? vertex-set v)]
          [v (into #{} (filter vertex-set) adj)])))

(defn- all-directed-graphs-over
  "Enumerates every directed graph (no self-loops) over the given vertices."
  [vertices]
  (let [ordered-pairs (for [u vertices v vertices :when (not= u v)] [u v])]
    (for [edges (bit-subsets ordered-pairs)]
      (reduce (fn [g [u v]] (update g u conj v))
              (zipmap vertices (repeat #{}))
              edges))))

(defn- all-directed-acyclic-graphs-over
  "Enumerates every DAG over the given vertices."
  [vertices]
  (filter acyclic? (all-directed-graphs-over vertices)))

(defn- all-undirected-graphs-over
  "Enumerates every undirected graph over the given vertices."
  [vertices]
  (let [unordered-pairs (set (for [u vertices v vertices
                                   :when (neg? (compare u v))]
                               #{u v}))]
    (for [edges (bit-subsets unordered-pairs)]
      (reduce (fn [g pair]
                (let [[u v] (seq pair)]
                  (-> g (update u conj v) (update v conj u))))
              (zipmap vertices (repeat #{}))
              edges))))

(defn- permutations
  "Returns a seq of all permutations of the given collection, built by
  inserting each element into every position of each shorter permutation."
  [coll]
  (reduce (fn insert-at-each-position [perms x]
            (for [p perms
                  i (range (inc (count p)))
                  :let [[before after] (split-at i p)]]
              (concat before [x] after)))
          [()]
          coll))

(defn- all-topological-orderings-of
  "Enumerates every valid topological ordering of the DAG."
  [graph]
  (let [es (edges graph)]
    (filter (fn [ordering]
              (let [index (zipmap ordering (range))]
                (every? (fn [[u v]] (< (index u) (index v))) es)))
            (permutations (keys graph)))))

(defn- all-ancestor-closed-subgraphs-of
  "Enumerates every ancestor-closed subgraph of the DAG. A vertex set is
  ancestor-closed when every edge u→v with v in the set also has u in the
  set — which is enough to enforce closure under the full ancestor chain."
  [graph]
  (let [es (edges graph)]
    (for [vs (bit-subsets (keys graph))
          :when (every? (fn [[u v]]
                          (or (not (contains? vs v))
                              (contains? vs u)))
                        es)]
      (induced-subgraph graph vs))))

(defspec gen-directed-acyclic-graph-covers-all-outputs
  (prop/for-all [vertices gen-coverage-vertex-set]
    (is (= (set (all-directed-acyclic-graphs-over vertices))
           (set (gen/sample (gen.graph/gen-directed-acyclic-graph vertices)
                            coverage-sample-count))))))

(defspec gen-directed-graph-covers-all-outputs
  (prop/for-all [vertices gen-coverage-vertex-set]
    (is (= (set (all-directed-graphs-over vertices))
           (set (gen/sample (gen.graph/gen-directed-graph vertices)
                            coverage-sample-count))))))

(defspec gen-undirected-graph-covers-all-outputs
  (prop/for-all [vertices gen-coverage-vertex-set]
    (is (= (set (all-undirected-graphs-over vertices))
           (set (gen/sample (gen.graph/gen-undirected-graph vertices)
                            coverage-sample-count))))))

(defspec gen-topological-ordering-covers-all-orderings
  (prop/for-all [graph gen-coverage-dag]
    (is (= (set (all-topological-orderings-of graph))
           (set (gen/sample (gen.graph/gen-topological-ordering graph)
                            coverage-sample-count))))))

(defspec gen-ancestor-closed-subgraph-covers-all-outputs
  (prop/for-all [graph gen-coverage-dag]
    (is (= (set (all-ancestor-closed-subgraphs-of graph))
           (set (gen/sample (gen.graph/gen-ancestor-closed-subgraph graph)
                            coverage-sample-count))))))

(deftest gen-pruned-directed-acyclic-graph-alias-test
  (is (identical? gen.graph/gen-ancestor-closed-subgraph
                  gen.graph/gen-pruned-directed-acyclic-graph)))
