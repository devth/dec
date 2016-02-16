<p align="center">
  <img width="560"
    src="https://github.com/devth/dec/raw/master/img/dec-colors.png?raw=true" />
</p>
<p align="center">
  <i>Deep Environmental Configuration</i>
</p>

# dec

dec builds arbitrarily nested data structures from simple KV strings.

- Tiny codebase; tiny name
- Functionally pure
- Composable
- Zero runtime dependencies
- 100% test coverage with unit tests & [test.check](https://github.com/clojure/test.check)
- Fully linted against [eastwood](https://github.com/jonase/eastwood) &
  [kibit](https://github.com/jonase/kibit)
- Continuously Delivered

:warning: Work in progress. :warning:

## Rationale

Env vars are constrained to simple key value pairs, yet they are ubiquitous.
Docker config and [12 Factor Config](http://12factor.net/config) in particular
rely on this traditional mechanism. But modern apps often require more complex
data structures, to wit: maps and lists.

## Usage

dec primary exposes two functions: `explode` and `enflat`. They are inverses of
eachother, such that `identity == (comp explode enflat)`, i.e.:

```clojure
(let [nested {:foo {:bar [:baz :qux]}}]
  (= nested (explode (enflat nested))))
;;=> true
```

Assume our env looks like:

```shell
export DEC_HOSTS_0=a.host.com
export DEC_HOSTS_1=b.host.com
export DEC_LEVEL=debug
```

Let's say we obtain some env vars via
[environ](https://github.com/weavejester/environ), as one does.
Let's filter by a known prefix, then explode the results:

```clojure
(require
  '[environ.core :refer [env]]
  '[dec :as dec])

(explode (into {} (filter (fn [[k v]] (.startsWith (name k) "dec")) env)))

{:dec {:hosts ["a.host.com" "b.host.com"], :level "debug"}}
```

## Run tests

```
lein test
```

## Calculate code coverage

```
lein cloverage
```

## Run linters

```
lein kibit
lein eastwood
```

## License

Copyright © 2016 Trevor C. Hartman

Distributed under the Eclipse Public License either version 1.0 or any later
version.
