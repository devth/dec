# dec Change Log

## 1.0.1 - 2/17/16

### Changed

- `enflat` and `explode` now take an options map to specify a custom delimiter
  instead of keyword args, e.g.:

  ```clojure
  (enflat {:foo.bar "1"} {:delimiter "."})
  ```
