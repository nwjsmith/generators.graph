(ns com.theinternate.generators.graph
  (:require [clojure.set :as set]
            [clojure.test.check.generators :as gen]))

(defn- map-vertices-indexed
  "Returns a matrix consisting applying f to each entry, its row coordinate, and
  its column coordinate."
  [f matrix]
  (into []
        (map-indexed
         (fn mapped-row [row-index row]
           (into []
                 (map-indexed (fn mapped-vertex [column-index vertex]
                                (f row-index column-index vertex)))
                 row)))
        matrix))

(defn- map-vertices
  "Returns a square matrix consisting of applying f to each entry."
  [f matrix]
  (map-vertices-indexed (fn mapped-indexed-vertex
                          [_ _ vertex] (f vertex)) matrix))

(defn- empty-matrix
  "Returns a square matrix containing an entry with a `nil` value for each of
  the number of vertices."
  [vertex-count]
  (vec (repeat vertex-count (vec (repeat vertex-count nil)))))

(def ^:private gen-probability
  (gen/double* {:min 0.0 :max 1.0 :NaN? false}))

(def ^:private gen-zero-probability
  (gen/return 0.0))

(defn- adjacent?
  "Returns true if the probability indicates the vertices are adjacent"
  [probability]
  (< 0.5 probability))

(defn- gen-acyclic-probability-matrix
  "Generates a matrix containing a probability of there being an edge between
  two vertices. The generated matrix will not contain any cycles. It will
  contain entries for the given number of vertices."
  [vertex-count]
  (apply gen/tuple
         (map (partial apply gen/tuple)
              (map-vertices-indexed (fn gen-probability-of-adjacent [row col _]
                                      (if (< col row)
                                        gen-probability
                                        gen-zero-probability))
                                    (empty-matrix vertex-count)))))

(defn- gen-acyclic-adjacency-matrix
  "Generates an adjacency matrix for the given number of vertices. No edges
  of the graph will form a cycle. Based on the algorithm described in Cordeiro,
  Daniel et al. Random Graph Generation for Scheduling Simulations."
  [vertex-count]
  (gen/fmap (fn truth-table [probability-matrix]
              (map-vertices adjacent? probability-matrix))
            (gen-acyclic-probability-matrix vertex-count)))

(defn- directed-graph
  "Returns the directed graph represented by the adjacency matrix and the given
  vertices."
  [vertices matrix]
  (reduce (fn vertex-joined [m [row column :as coordinates]]
            (if (get-in matrix coordinates)
              (update m (nth vertices column) conj (nth vertices row))
              m))
          (zipmap vertices (repeat #{}))
          (for [row (range 0 (count vertices))
                column (range 0 (count vertices))]
            [row column])))

(defn gen-directed-acyclic-graph
  "Generates a random directed, acyclic graph containing the given vertices."
  [vertices]
  (gen/fmap (fn adjacency-list [[shuffled-vertices matrix]]
              (directed-graph shuffled-vertices matrix))
            (gen/tuple (gen/shuffle vertices)
                       (gen-acyclic-adjacency-matrix (count vertices)))))

(defn- vertices
  "Returns a set containing all of the directed graph's vertices."
  [directed-graph]
  (set (keys directed-graph)))

(defn- roots
  "Returns the set of vertices in the graph which have no incoming edges."
  [directed-graph]
  (into #{}
        (filter (fn root? [vertex]
                  (not-any? (fn adjacent? [adjacents]
                              (contains? adjacents vertex))
                            (vals directed-graph))))
        (vertices directed-graph)))

(defn- parent?
  "Returns true if the directed graph contains an edge from the vertex to the
  child."
  [directed-graph vertex child]
  (contains? (get directed-graph vertex) child))

(defn- interchange
  "Swaps the values at two indices in the associative collection."
  [coll index-one index-two]
  (assoc coll
         index-one (nth coll index-two)
         index-two (nth coll index-one)))

(defn- topological-ordering
  "Returns a topological ordering of the vertices in the directed, acyclic
  graph."
  [directed-acyclic-graph]
  (loop [ordered []
         remaining-roots (roots directed-acyclic-graph)
         remaining-graph directed-acyclic-graph]
    (if (seq remaining-roots)
      (let [root (first remaining-roots)
            graph-without-root (dissoc remaining-graph root)]
        (recur (conj ordered root)
               (set/union (disj remaining-roots root)
                          (roots graph-without-root))
               graph-without-root))
      ordered)))

(defn gen-topological-ordering
  "Generates a seq of all vertices in the directed, acyclic graph. The seq will
  be in topological order."
  [directed-acyclic-graph]
  (let [vertex-count (count directed-acyclic-graph)]
    (gen/fmap
     (fn interchanged-at-indices [random-indices]
       (reduce (fn interchanged-at-index [ordered-vertices random-index]
                 (let [next-index (inc random-index)]
                   (if (and (< next-index vertex-count)
                            (not (parent? directed-acyclic-graph
                                          (nth ordered-vertices random-index)
                                          (nth ordered-vertices next-index))))
                     (interchange ordered-vertices random-index next-index)
                     ordered-vertices)))
               (topological-ordering directed-acyclic-graph)
               random-indices))
     (gen/vector (gen/choose 0 (dec vertex-count))
                 (* 10 (Math/pow vertex-count 2))))))

(defn- keep-vertices
  "Returns a directed graph containing only the given vertices."
  [vertices directed-graph]
  (let [vertex? (set vertices)]
    (into {}
          (comp (filter (comp vertex? key))
                (map (fn entry-without-vertices [[vertex adjacents]]
                       [vertex (into #{} (filter vertex?) adjacents)])))
          directed-graph)))

(defn gen-pruned-directed-acyclic-graph
  "Generates a subgraph of the directed, acyclic graph. Each of the subgraph's
  vertices' ancestors are also in the graph. The subgraph will contain at least
  the minimum number of vertices (default 0)."
  ([directed-acyclic-graph]
   (gen-pruned-directed-acyclic-graph directed-acyclic-graph
                                      {:minimum-vertex-count 0}))
  ([directed-acyclic-graph {:keys [minimum-vertex-count]}]
   (gen/bind
    (gen-topological-ordering directed-acyclic-graph)
    (fn pruned-from-end [ordering]
      (gen/fmap (fn vertices-removed [n]
                  (keep-vertices (drop-last n ordering) directed-acyclic-graph))
                (gen/choose 0 (max 0 (- (count ordering)
                                        minimum-vertex-count))))))))
