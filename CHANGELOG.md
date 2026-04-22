# Unreleased

## Added

- `gen-directed-graph` generator for random directed graphs, which may contain
  cycles.
- `gen-undirected-graph` generator for random undirected graphs.
- `gen-weighted` generator that decorates the edges of a graph with
  non-negative integer weights.

## Fixed

- Edge probabilities are now sampled uniformly from [0.0, 1.0]. Previously
  the underlying `gen/double*` biased heavily toward small values, which
  made generated graphs skew sparse — e.g., the fully-connected directed
  graph on three vertices occurred with probability ~10⁻⁸ instead of the
  intended 1/64.

## Changed

- Renamed `gen-pruned-directed-acyclic-graph` to `gen-ancestor-closed-subgraph`
  to name the invariant it guarantees. The old name is retained as a
  deprecated alias.
- `gen-ancestor-closed-subgraph` now preserves weighted edges on its input,
  so it composes with `gen-weighted` in either order.
- Updated Clojure dependency from 1.10.1 to 1.10.2
- Updated test.check dependency from 0.10.0 to 1.1.0

# 0.0-45 (2019-10-03 / 994c1f2)

## Added

## Fixed

## Changed

- Updated Clojure dependency from 1.10.0 to 1.10.1
- Updated test.check dependency from 0.9.0 to 0.10.0

# 0.0-41 (2019-04-30 / ec7b9a9)

## Changed

- Named all functions. This will cause them to show up properly in stacktraces.

# 0.0-37 (2019-02-27 / 82cdcb7)

## Added

- ClojureScript support

## Fixed

## Changed

- API reference published to cljdoc.org on each release

# 0.0-29 (2019-02-17 / 6f45b36)

## Added

- First release, with generators for DAGs, topological orderings, and pruned
  DAGs.

## Fixed

## Changed
