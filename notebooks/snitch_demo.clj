(ns snitch-demo
  (:require [snitch.core :refer [defn* defmethod* *fn *let]]
            [scicloj.kindly.v4.kind :as kind]))

;; If I have a function like this(I know it is silly, hard to come up with example).  
;; and I need to know what this function do, just imagine this is a very complex function
(defn some-f [x y]
  (let [z (+ x y)
        w (* 2 (inc z))
        v (+ 42 z w)]
    v))

;; first I call this function with some parameters
(some-f 1 2)

;; now if I want to see the value of local variables, for example `w`  
;; I can't just evaluate `(* 2 (inc z))`, because `z` is not in the current namespace, I need to define `z`    
;; but to define `z`, I need `x` and `y`, they are also not in the current namespace  
;; for some simple function, I can define each of the local variables  
;; this require many manual work, basiclly copy every step of the function to top level.  
(def x 1)
(def y 2)
(def z (+ x y))
(def w (* 2 (inc z)))
(def v (+ 42 z w))

;; but this is not scalable for more complex functions.  
;; and also subject to change, it needs to be manually synchronized with the original function  

;; an alternative way is to use inline def, it is now synchronized with the original function, but 
;; need to be reverted back when done debugging
(defn some-f [x y]
  (def z (+ x y))
  (def w (* 2 (inc z)))
  (def v (+ 42 z w))
  v)

;; print is also valuable, it is simple to use, but still requires manual cleanup
(defn some-f [x y]
  (let [z (+ x y)
        w (* 2 (inc z))
        v (+ 42 z w)]
    (println "z" z)
    (println "w" w)
    (println "v" v)
    v))

;; snicht library here provide some macro that will perform inline def, easy to setup and cleanup.
;; just change defn to defn*
(defn* some-f1 [x y]
  (let [z (+ x y)
        w (* 2 (inc z))
        v (+ 42 z w)]
    v))

;; what this defn* macro do is when this function is evaluate, it will inline-def all the local variable
;; and arguments to top-level
;; just like user have written 
(defn some-f [x y]
  (def z (+ x y))
  (def w (* 2 (inc z)))
  (def v (+ 42 z w))
  v)

;; let's give it a try
(some-f1 1 2)
{:x x
 :y y
 :z z
 :w w
 :v v}

;; but note that, it suffer the same problem as manually written inline-def.  
;; def can pollute top level namespace.
;; for example z was bind to 42 before eval some-f1
(def z 42)
z

;; after evaluation, the value changed
(some-f1 1 2)
z