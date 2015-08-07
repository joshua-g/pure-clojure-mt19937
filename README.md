#### Usage

Download and run `lein install` from this project's root directory.

In your project.clj, add `[pure-clojure-mt19937 "0.1.0"]` to the :dependencies vector.

When using, `:require [joshua-g.mersenne :as mt]` within your `ns` declaration, or `(require '[joshua-g.mersenne :as mt])` from the REPL.

##### Examples

```
user> (require '[joshua-g.mersenne :as mt])
nil

user> (take 5 (mt/mersenne 1))
(1791095845 4282876139 3093770124 4005303368 491263)

user> (let [[values new-mt] (mt/next-values 5 (mt/mersenne 1))]
        (println values)
        (println (take 5 new-mt)))
[1791095845 4282876139 3093770124 4005303368 491263]
(550290313 1298508491 4290846341 630311759 1013994432)
```
