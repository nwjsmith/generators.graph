# generators.graph

[![CircleCI](https://circleci.com/gh/nwjsmith/generators.graph.svg?style=svg)](https://circleci.com/gh/nwjsmith/generators.graph) [![cljdoc badge](https://cljdoc.org/badge/com.theinternate/generators.graph)](https://cljdoc.org/d/com.theinternate/generators.graph) [![Clojars Project](https://img.shields.io/clojars/v/com.theinternate/generators.graph.svg)](https://clojars.org/com.theinternate/generators.graph) [![codecov](https://codecov.io/gh/nwjsmith/generators.graph/branch/master/graph/badge.svg)](https://codecov.io/gh/nwjsmith/generators.graph)

`generators.graph` is a library of [`test.check`](https://github.com/clojure/test.check) generators for graph data.

## Install

**tools.deps**

```clojure
com.theinternate/generators.graph {:mvn/version "0.0-29"}
```

**Leiningen/Boot**

```clojure
[com.theinternate/generators.graph "0.0-29"]
```

**Maven**

```xml
<dependency>
  <groupId>com.theinternate</groupId>
  <artifactId>generators.graph</artifactId>
  <version>0.0-29</version>
</dependency>
```

## Usage

## API

The public API is provided by the `com.theinternate.generators.graph` namespace.

`(gen-directed-acyclic-graph vertices)`

Generates a random directed, acyclic graph containing the given vertices.

**Examples**

```clojure
(require '[clojure.test.check.generators :as gen])
(require '[com.theinternate.generators.graph :as gen.graph])
(gen/generate (gen.graph/gen-directed-acyclic-graph #{:a :b :c :d :e}))
;; =>
{:a #{:b}
 :b #{:c}
 :c #{}
 :d #{:b :c}
 :e #{}}
```

`(gen-topological-ordering directed-acyclic-graph)`

Generates a seq of all vertices in the directed, acyclic graph. The seq will be in topological order.

**Examples**

```clojure
(require '[clojure.test.check.generators :as gen])
(require '[com.theinternate.generators.graph :as gen.graph])
(gen/generate (gen.graph/gen-topological-ordering {:a #{:b}
                                                   :b #{:c}
                                                   :c #{}
                                                   :d #{:b :c}
                                                   :e #{}}))
;; =>
[:d :a :b :c :e]
```

`(gen-pruned-directed-acyclic-graph directed-acyclic-graph)`
`(gen-pruned-directed-acyclic-graph directed-acyclic-graph options)`

Generates a subgraph of the directed, acyclic graph. Each of the subgraph's vertices' ancestors are also in the graph.

**Options**

`:minimum-vertex-count` - The minimum number of vertices generated subgraphs must contain. Default `0`.

**Examples**

```clojure
(require '[clojure.test.check.generators :as gen])
(require '[com.theinternate.generators.graph :as gen.graph])
(gen/generate (gen.graph/gen-pruned-directed-acyclic-graph {:a #{:b}
                                                            :b #{:c}
                                                            :c #{}
                                                            :d #{:b :c}
                                                            :e #{}}))
;; =>
{:a #{}
 :d #{}
 :e #{}}

(gen/generate
  (gen.graph/gen-pruned-directed-acyclic-graph {:a #{:b}
                                                :b #{:c}
                                                :c #{}
                                                :d #{:b :c}
                                                :e #{}}
                                               {:minimum-vertex-count 4}))
;; =>
{:a #{:b}
 :b #{}
 :d #{:b}
 :e #{}}
```

## Maintainer

[Nate Smith](http://theinternate.com)

## License

Copyright © 2019 Nathaniel Smith

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
