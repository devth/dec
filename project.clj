(defproject ec "1.0.0"
  :description "A tiny, pure library for encoding maps and lists in K/V config."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [environ "1.0.2"]]
                   :plugins [[lein-cloverage "1.0.6"]
                             [jonase/eastwood "0.2.3"]
                             [lein-kibit "0.1.2"]]}}
  :dependencies [[org.clojure/clojure "1.7.0"]])
