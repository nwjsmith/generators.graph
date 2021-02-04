# generators.graph

[![GitHub Actions badge](https://github.com/nwjsmith/generators.graph/workflows/Test/badge.svg)](https://github.com/nwjsmith/generators.graph/actions?query=workflow%3ATest+branch%3Amaster) [![cljdoc badge](https://cljdoc.org/badge/com.theinternate/generators.graph)](https://cljdoc.org/d/com.theinternate/generators.graph) [![Clojars Project](https://img.shields.io/clojars/v/com.theinternate/generators.graph.svg)](https://clojars.org/com.theinternate/generators.graph) [![Codecov badge](https://codecov.io/gh/nwjsmith/generators.graph/branch/master/graph/badge.svg)](https://codecov.io/gh/nwjsmith/generators.graph)

`generators.graph` is a Clojure(Script) library of [`test.check`](https://github.com/clojure/test.check) generators for graph data.

## Install

**tools.deps**

```clojure
com.theinternate/generators.graph {:mvn/version "0.0-45"}
```

**Leiningen/Boot**

```clojure
[com.theinternate/generators.graph "0.0-45"]
```

**Maven**

```xml
<dependency>
  <groupId>com.theinternate</groupId>
  <artifactId>generators.graph</artifactId>
  <version>0.0-45</version>
</dependency>
```

## Usage

Fire up a REPL and try it out!

```clojure
(require '[clojure.test.check.generators :as gen])
(require '[com.theinternate.generators.graph :as gen.graph])
```

Generate a random DAG from a set of nodes:

```clojure
(gen/generate (gen.graph/gen-directed-acyclic-graph #{:a :b :c :d}))
```

Generate a random topological ordering of a DAG:

```clojure
(gen/generate (gen.graph/gen-topological-ordering {:a #{}
                                                   :b #{}
                                                   :c #{:b :d}
                                                   :d #{:a}}))
```

Generate a pruned subgraph of a DAG:

```clojure
(gen/generate (gen.graph/gen-pruned-directed-acyclic-graph {:a #{}
                                                            :b #{:a}
                                                            :c #{:b :d :a}
                                                            :d #{:b :a}
                                                            :e #{:c :b :a}}))
```

## API reference

[API reference documentation](https://cljdoc.org/d/com.theinternate/generators.graph/CURRENT) is hosted by [cljdoc](https://cljdoc.org).

## Maintainer

[Nate Smith](http://theinternate.com)

## License

Copyright © 2019 Nathaniel Smith

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
