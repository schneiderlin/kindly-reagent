(ns test1
  (:require
   [scicloj.kindly.v4.kind :as kind]))

;; values
"str"
1
1.1
:keyword
nil
true
[1 2 3]
{:a 1 :b 2}

;; tex
(kind/tex "x^2=\\alpha")

;; md
(kind/md
 "If $x$ equals 9, then $$x^2+9=90$$")

;; hidden
(kind/hidden
 {:x 9})

;; code
(kind/code "(def x 1)")

;; image
(-> "https://upload.wikimedia.org/wikipedia/commons/e/eb/Ash_Tree_-_geograph.org.uk_-_590710.jpg"
    (java.net.URL.)
    (javax.imageio.ImageIO/read)
    kind/image)

;; reagent
(kind/reagent
 ['(fn [{:keys [initial-value
                background-color]}]
     (let [*click-count (reagent.core/atom initial-value)]
       (fn []
         [:div {:style {:background-color background-color}}
          "The atom " [:code "*click-count"] " has value: "
          @*click-count ". "
          [:input {:type "button" :value "Click me!"
                   :on-click #(swap! *click-count inc)}]])))
  {:initial-value 9
   :background-color "#d4ebe9"}])

;; echarts
(kind/echarts
 {:title {:text "Echarts Example"}
  :tooltip {}
  :legend {:data ["sales"]}
  :xAxis {:data ["Shirts", "Cardigans", "Chiffons",
                 "Pants", "Heels", "Socks"]}
  :yAxis {}
  :series [{:name "sales"
            :type "bar"
            :data [5 20 36
                   10 10 20]}]})

;; vega-lite
(kind/vega-lite
 {:encoding
  {:y {:field "y", :type "quantitative"},
   :size {:value 400},
   :x {:field "x", :type "quantitative"}},
  :mark {:type "circle", :tooltip true},
  :width 400,
  :background "floralwhite",
  :height 100,
  :data {:values "x,y\n1,1\n2,-4\n3,9\n", :format {:type "csv"}}})

;; highcharts
(kind/highcharts
 {:title {:text "Line chart"}
  :subtitle {:text "By Job Category"}
  :yAxis {:title {:text "Number of Employees"}}
  :series [{:name "Installation & Developers"
            :data [43934, 48656, 65165, 81827, 112143, 142383,
                   171533, 165174, 155157, 161454, 154610]}

           {:name "Manufacturing",
            :data [24916, 37941, 29742, 29851, 32490, 30282,
                   38121, 36885, 33726, 34243, 31050]}

           {:name "Sales & Distribution",
            :data [11744, 30000, 16005, 19771, 20185, 24377,
                   32147, 30912, 29243, 29213, 25663]}

           {:name "Operations & Maintenance",
            :data [nil, nil, nil, nil, nil, nil, nil,
                   nil, 11164, 11218, 10077]}

           {:name "Other",
            :data [21908, 5548, 8105, 11248, 8989, 11816, 18274,
                   17300, 13053, 11906, 10073]}]

  :xAxis {:accessibility {:rangeDescription "Range: 2010 to 2020"}}

  :legend {:layout "vertical",
           :align "right",
           :verticalAlign "middle"}

  :plotOptions {:series {:label {:connectorAllowed false},
                         :pointStart 2010}}

  :responsive {:rules [{:condition {:maxWidth 500},
                        :chartOptions {:legend {:layout "horizontal",
                                                :align "center",
                                                :verticalAlign "bottom"}}}]}})

;; cytoscape
(kind/cytoscape
 {:elements {:nodes [{:data {:id "a" :parent "b"} :position {:x 215 :y 85}}
                     {:data {:id "b"}}
                     {:data {:id "c" :parent "b"} :position {:x 300 :y 85}}
                     {:data {:id "d"} :position {:x 215 :y 175}}
                     {:data {:id "e"}}
                     {:data {:id "f" :parent "e"} :position {:x 300 :y 175}}]
             :edges [{:data {:id "ad" :source "a" :target "d"}}
                     {:data {:id "eb" :source "e" :target "b"}}]}
  :style [{:selector "node"
           :css {:content "data(id)"
                 :text-valign "center"
                 :text-halign "center"}}
          {:selector "parent"
           :css {:text-valign "top"
                 :text-halign "center"}}
          {:selector "edge"
           :css {:curve-style "bezier"
                 :target-arrow-shape "triangle"}}]
  :layout {:name "preset"
           :padding 5}})

;; plotly
(kind/plotly
 (let [n 20
       walk (fn [bias]
              (->> (repeatedly n #(-> (rand)
                                      (- 0.5)
                                      (+ bias)))
                   (reductions +)))]
   {:data [{:x (walk 1)
            :y (walk -1)
            :z (map #(* % %)
                    (walk 2))
            :type :scatter3d
            :mode :lines+markers
            :opacity 0.2
            :line {:width 10}
            :marker {:size 20
                     :colorscale :Viridis}}]}))

;; # table
(kind/table {:x (range 6)
             :y [:A :B :C :A :B :C]})

;; video
(kind/video
 {:youtube-id "DAQnvAgBma8"})