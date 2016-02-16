(ns dec
  "Core dec functions"
  (:require
    [clojure.string :as s]))

(def default-delimiter "-")

;; private utilities

(defn- path->key
  "Encode a path sequential as a Clojure keyword"
  [delimiter path]
  {:pre [(not-empty path)
         (not-empty delimiter)]}
  (keyword (s/join delimiter (map #(if (number? %) % (name %)) path))))

(defn- flat? [v]
  "Test whether a value is flat, i.e. not a collection of values."
  (not (coll? v)))

(defn- ensure-min-vec-size [v size]
  "Ensure v has at least size elements. Fill with `nil` if necessary."
  (let [d (- size (count v))]
    (if (pos? d) (into v (vec (repeat d nil))) v)))

(defn- find-or-create
  "Build up nested path with possibly-numeric indices as vectors and other keys
   as maps"
  [m [k1 k2 & path]]
  (let [ds (cond-> (get-in m [k1] (if (number? k2) (vec (repeat k2 nil)) {}))
             ;; if k2 is a number we need to ensure the vector is large enough
             (number? k2) (ensure-min-vec-size k2)
             ;; recurse if path is non nil
             path (find-or-create (into [k2] path)))]
    (update-in m [k1] (constantly ds))))

(defn- update-in-idx
  "Like clojure.core/update-in except creates vectors insated of hash-maps when
   key is a number"
  [m path v]
  (update-in (find-or-create m path) path (constantly v)))

(defn- exploded?
  "Determine whether a key has been fully exploded"
  [re-delimiter k]
  (not (re-find re-delimiter k)))

;; public API

(defn enflat
  "Encode a nested data structure as a flat map; the inverse of explode."
  [data-structure & {:keys [delimiter] :or {delimiter default-delimiter}}]
  (letfn
    [(inner-flatten [d path]
       ;; sequences are paired with indexes; maps are naturally indexed by key
       (let [indexed (if (map? d) d (map-indexed vector d))]
         ;; conj each key and recursively flatten
         (map
           (fn [[k v]]
             (if (flat? v)
               [(path->key delimiter (conj path k)) v]
               (apply concat (inner-flatten v (conj path k)))))
           indexed)))]
    (apply hash-map (apply concat (inner-flatten data-structure [])))))

(defn explode
  "Explode a flat map into a nested data structure; the inverse of enflat."
  [flat-data-structure & {:keys [delimiter] :or {delimiter default-delimiter}}]
  (let [re-delimiter (re-pattern (str "\\" delimiter))]
    (reduce
      (fn [acc [k v]]
        (if (exploded? re-delimiter (name k))
          (assoc acc k v)
          (let [path (map #(let [k (read-string %)]
                             (if (number? k) k (keyword k)))
                          (s/split (name k) re-delimiter))]
            (update-in-idx acc path v))))
      {} flat-data-structure)))
