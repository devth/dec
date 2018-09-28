(defproject dec "1.0.2-SNAPSHOT"
  :description "A tiny, pure library for encoding maps and lists in K/V config."
  :url "https://github.com/devth/dec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [environ "1.0.2"]]
                   :plugins [[lein-cloverage "1.0.7-SNAPSHOT"]
                             [jonase/eastwood "0.2.3"]
                             [lein-kibit "0.1.2"]]}}
  :dependencies [[org.clojure/clojure "1.9.0"]])
