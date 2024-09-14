(ns main
  (:require [scicloj.clay.v2.notebook :as notebook]
            [scicloj.clay.v2.api :as clay]
            [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.make :refer [extract-specs]]
            [hiccup.compiler :refer [normalize-element]]))

(defn- get-tag [hiccup]
  (let [[tag _ _] (normalize-element hiccup)]
    tag))

(defn handle-script [hiccup]
  (let [[tag attrs children] (normalize-element hiccup)
        [non-script-nodes script-nodes] (split-with #(or (not (vector? %))
                                                         (not= (get-tag %) "script"))
                                                    children)]
    (if (empty? script-nodes)
      ;; no script tag in children, do nothing
      hiccup
      ;; wrap script tag using wrap-script
      [:f> 'wrap-scripts
       {:scripts (vec script-nodes)}
       ;; keep only non-script nodes
       (if (empty? non-script-nodes)
         [tag attrs]
         [tag attrs (vec non-script-nodes)])])))

(defn handle-code
  "all the code tag will 
   need to wrap in dangerouslySetInnerHTML"
  [hiccup]
  (let [[tag attrs children] (normalize-element hiccup)]
    (if (= tag "code")
      [:code (merge attrs
                    {:dangerouslySetInnerHTML
                     {:__html (first children)}})]
      hiccup)))

(defn- need-walk? [hiccup]
  (and (vector? hiccup)
       (not (map-entry? hiccup))))

(defn hiccup->reagent [hiccup]
  (clojure.walk/postwalk
   (fn [hiccup]
     (if (need-walk? hiccup)
       (-> hiccup
           handle-code
           handle-script)
       hiccup))
   hiccup))

(defn reagent
  "convert clay items to reagent"
  [items]
  (map (fn [{:as context
             :keys [hiccup html md
                    script
                    item-class]}]
         (cond
           hiccup (hiccup->reagent hiccup)
           md [:p md]
           :else
           nil))
       items))

(comment
  ;; -------- clay ---------------
  (def spec {:source-path "notebooks/test1.clj"})
  (def single-ns-specs (:single-ns-specs (extract-specs (config/config) spec)))
  (def spec1 (first single-ns-specs))
  (def result (notebook/items-and-test-forms spec1))
  (def items (:items result))
  ;; -------- end of clay ---------------

  ;; the output of this can be copy to demo/scittle.cljs my-component
  (reagent items)
  
  :rcf)