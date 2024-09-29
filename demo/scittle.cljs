(require '[reagent.dom :as rdom])

(defn- get-tag [hiccup]
  (if (vector? hiccup)
    (if (map? (second hiccup))
      (let [[tag attrs & children] hiccup]
        tag)
      (let [[tag & children] hiccup]
        tag))
    (do
      (println "not a vector? " hiccup)
      nil)))

(defn- get-attr [hiccup]
  (if (vector? hiccup)
    (if (map? (second hiccup))
      (let [[tag attrs & children] hiccup]
        attrs)
      (let [[tag & children] hiccup]
        {}))
    nil))

(defn- get-children [hiccup]
  (if (vector? hiccup)
    (if (map? (second hiccup))
      (let [[tag attrs & children] hiccup]
        children)
      (let [[tag & children] hiccup]
        children))
    []))

(defn use-script
  "a custom hook to add script tags to the dom after the component rendered.
   scripts is a collection of hiccup form [:script {:src '...' :type '...'} js-code]
   this function return a ref, pass this ref to the dom element you want to add script tags."
  [{:keys [scripts]}]
  (let [ref (js/React.useRef)
        _ (js/React.useEffect
           (fn []
             (when (.-current ref) ;; when the DOM element contains this ref is rendered
               (let [;; create script elements
                     script-elems
                     (map (fn [_x]
                            (js/document.createElement "script")) scripts)]
                 ;; set attributes and content for each script element, then add to the dom
                 (doseq [[script-elem script] (map vector script-elems scripts)]
                   (let [attrs (get-attr script)
                         content (first (get-children script))]
                     (doseq [[attr val] attrs]
                       (.setAttribute script-elem (name attr) val))
                     (set! (.-textContent script-elem) content)
                     (.appendChild (.-current ref) script-elem)))
                 ;; clean up
                 (fn []
                   (doseq [script-elem script-elems]
                     (.removeChild (.-current ref) script-elem))))))
           [scripts ref])]
    ref))

(defn wrap-scripts
  "given a hiccup form, add script tags to the root"
  [{:keys [scripts] :as props} & children]
  (let [ref (use-script {:scripts scripts})
        child (first children)]
    (if (nil? child)
      [:div {:ref ref}]
      (let [tag (get-tag child)
            new-attrs (assoc (get-attr child) :ref ref)
            new-child [tag new-attrs (get-children child)]]
        new-child))))

(defn my-component []
  [:div
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure source-clojure bg-light",
       :dangerouslySetInnerHTML {:__html "(ns test1\n  (:require\n   [scicloj.kindly.v4.kind :as kind]))"}}]]]
   [:p "values"]
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure source-clojure bg-light",
       :dangerouslySetInnerHTML {:__html "&quot;str&quot;"}}]]]
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "&quot;str&quot;\r\n"}}]]]
   [:div
    [:pre
     [:code {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html "1"}}]]]
   [:div
    [:pre [:code {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "1\r\n"}}]]]
   [:div
    [:pre
     [:code {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html "1.1"}}]]]
   [:div
    [:pre [:code {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "1.1\r\n"}}]]]
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html ":keyword"}}]]]
   [:div
    [:pre
     [:code {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html ":keyword\r\n"}}]]]
   [:div
    [:pre
     [:code {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html "nil"}}]]]
   [:div
    [:pre [:code {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "nil\r\n"}}]]]
   [:div
    [:pre
     [:code {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html "true"}}]]]
   [:div
    [:pre [:code {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "true\r\n"}}]]]
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html "[1 2 3]"}}]]]
   [:div
    [:pre
     [:code {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "[1 2 3]\r\n"}}]]]
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure printed-clojure", :dangerouslySetInnerHTML {:__html "{:a 1, :b 2}\r\n"}}]]]
   [:p "tex"]
   [:div
    [:f>
     wrap-scripts
     {:scripts
      [[:script "katex.render(\"x^2=\\\\alpha\", document.currentScript.parentElement, {throwOnError: false});"]]}
     ["div" {}]]]
   [:p "md"]
   [:p "If $x$ equals 9, then $$x^2+9=90$$"]
   [:p "hidden"]
   ""
   [:p "code"]
   [:div
    [:pre
     [:code
      {:class "sourceCode language-clojure source-clojure bg-light", :dangerouslySetInnerHTML {:__html "(def x 1)"}}]]]
   [:p "image"]
   [:img {:src "test1_files/0.png"}]
   [:p "reagent"]
   [:f>
    wrap-scripts
    {:scripts
     [[:script
       {:type "application/x-scittle"}
       "(reagent.dom/render [(fn [{:keys [initial-value background-color]}] (let [*click-count (reagent.core/atom initial-value)] (fn [] [:div {:style {:background-color background-color}} \"The atom \" [:code \"*click-count\"] \" has value: \" (clojure.core/deref *click-count) \". \" [:input {:type \"button\", :value \"Click me!\", :on-click (fn* [] (swap! *click-count inc))}]]))) {:initial-value 9, :background-color \"#d4ebe9\"}] (js/document.getElementById \"id1\"))"]]}
    ["div" {:id "id1"}]]
   [:p "echarts"]
   [:f>
    wrap-scripts
    {:scripts
     [[:script
       "\n{\n  var myChart = echarts.init(document.currentScript.parentElement);\n  myChart.setOption({\"title\":{\"text\":\"Echarts Example\"},\"tooltip\":{},\"legend\":{\"data\":[\"sales\"]},\"xAxis\":{\"data\":[\"Shirts\",\"Cardigans\",\"Chiffons\",\"Pants\",\"Heels\",\"Socks\"]},\"yAxis\":{},\"series\":[{\"name\":\"sales\",\"type\":\"bar\",\"data\":[5,20,36,10,10,20]}]});\n};"]]}
    ["div" {:style {:height "400px", :width "100%"}}]]
   [:p "vega-lite"]
   [:f>
    wrap-scripts
    {:scripts
     [[:script
       "vegaEmbed(document.currentScript.parentElement, {\"encoding\":{\"y\":{\"field\":\"y\",\"type\":\"quantitative\"},\"size\":{\"value\":400},\"x\":{\"field\":\"x\",\"type\":\"quantitative\"}},\"mark\":{\"type\":\"circle\",\"tooltip\":true},\"width\":400,\"background\":\"floralwhite\",\"height\":100,\"data\":{\"url\":\"test1_files\\/1.csv\",\"format\":{\"type\":\"csv\"}}});"]]}
    ["div" {}]]
   [:p "highcharts"]
   [:f>
    wrap-scripts
    {:scripts
     [[:script
       "Highcharts.chart(document.currentScript.parentElement, {\"title\":{\"text\":\"Line chart\"},\"subtitle\":{\"text\":\"By Job Category\"},\"yAxis\":{\"title\":{\"text\":\"Number of Employees\"}},\"series\":[{\"name\":\"Installation & Developers\",\"data\":[43934,48656,65165,81827,112143,142383,171533,165174,155157,161454,154610]},{\"name\":\"Manufacturing\",\"data\":[24916,37941,29742,29851,32490,30282,38121,36885,33726,34243,31050]},{\"name\":\"Sales & Distribution\",\"data\":[11744,30000,16005,19771,20185,24377,32147,30912,29243,29213,25663]},{\"name\":\"Operations & Maintenance\",\"data\":[null,null,null,null,null,null,null,null,11164,11218,10077]},{\"name\":\"Other\",\"data\":[21908,5548,8105,11248,8989,11816,18274,17300,13053,11906,10073]}],\"xAxis\":{\"accessibility\":{\"rangeDescription\":\"Range: 2010 to 2020\"}},\"legend\":{\"layout\":\"vertical\",\"align\":\"right\",\"verticalAlign\":\"middle\"},\"plotOptions\":{\"series\":{\"label\":{\"connectorAllowed\":false},\"pointStart\":2010}},\"responsive\":{\"rules\":[{\"condition\":{\"maxWidth\":500},\"chartOptions\":{\"legend\":{\"layout\":\"horizontal\",\"align\":\"center\",\"verticalAlign\":\"bottom\"}}}]}});"]]}
    ["div" {}]]
   [:p "cytoscape"]
   [:f>
    wrap-scripts
    {:scripts
     [[:script
       "\n{\n  value = {\"elements\":{\"nodes\":[{\"data\":{\"id\":\"a\",\"parent\":\"b\"},\"position\":{\"x\":215,\"y\":85}},{\"data\":{\"id\":\"b\"}},{\"data\":{\"id\":\"c\",\"parent\":\"b\"},\"position\":{\"x\":300,\"y\":85}},{\"data\":{\"id\":\"d\"},\"position\":{\"x\":215,\"y\":175}},{\"data\":{\"id\":\"e\"}},{\"data\":{\"id\":\"f\",\"parent\":\"e\"},\"position\":{\"x\":300,\"y\":175}}],\"edges\":[{\"data\":{\"id\":\"ad\",\"source\":\"a\",\"target\":\"d\"}},{\"data\":{\"id\":\"eb\",\"source\":\"e\",\"target\":\"b\"}}]},\"style\":[{\"selector\":\"node\",\"css\":{\"content\":\"data(id)\",\"text-valign\":\"center\",\"text-halign\":\"center\"}},{\"selector\":\"parent\",\"css\":{\"text-valign\":\"top\",\"text-halign\":\"center\"}},{\"selector\":\"edge\",\"css\":{\"curve-style\":\"bezier\",\"target-arrow-shape\":\"triangle\"}}],\"layout\":{\"name\":\"preset\",\"padding\":5}};\n  value['container'] = document.currentScript.parentElement;\n  cytoscape(value);\n};"]]}
    ["div" {:style {:height "400px", :width "100%"}}]]
   [:p "plotly"]
   [:f>
    wrap-scripts
    {:scripts
     [[:script
       "Plotly.newPlot(document.currentScript.parentElement,\n              [{\"x\":[0.5899680909460149,1.434335886342899,2.15716341945855,2.754137499947145,3.7957935658937085,4.632897253346378,5.134515523967748,6.200815036983195,6.770000106793651,7.644873184530217,8.88815387097793,9.411419295893424,10.377802151101353,11.7201087339562,12.463544684075444,13.012519583835115,13.92469297490491,15.086021703874342,16.13725642453741,16.86744782702835],\"y\":[-1.1204677587089433,-2.275760982815074,-2.911238171822459,-3.9477777318002056,-5.140929449046203,-5.778409231364332,-6.420082216386733,-7.8629300772419874,-8.63369808793628,-9.867292719188272,-10.882759545209236,-11.821616336821394,-13.253328944035585,-14.134359599876436,-15.587129044726709,-16.84010895066223,-17.77901830455586,-18.86946260822027,-19.963414778881134,-20.777315163463268],\"z\":[5.629210298511083,17.432127304168716,37.99597846413235,73.82787656476073,118.21621708854336,172.49417001816227,241.48175607494986,303.1397906499304,374.72426445521523,469.69752143893544,553.2239723132926,652.790004801442,770.0828029854146,855.7806439508579,984.7281540564786,1117.992555640754,1251.3500518320018,1382.792217945249,1548.333822853893,1715.145522052831],\"type\":\"scatter3d\",\"mode\":\"lines+markers\",\"opacity\":0.2,\"line\":{\"width\":10},\"marker\":{\"size\":20,\"colorscale\":\"Viridis\"}}], {}, {});"]]}
    ["div" {:style {:height "400px", :width "100%"}}]]
   [:p "\n# table"]
   [:table
    {:class "table table-hover table-responsive"}
    [:thead [:tr [:th :x] [:th :y]]]
    [:tbody
     [:tr [:td 0] [:td :A]]
     [:tr [:td 1] [:td :B]]
     [:tr [:td 2] [:td :C]]
     [:tr [:td 3] [:td :A]]
     [:tr [:td 4] [:td :B]]
     [:tr [:td 5] [:td :C]]]]
   [:p "video"]
   [:iframe {:src "https://www.youtube.com/embed/DAQnvAgBma8", :allowfullscreen true}]
   [:div {:style {:height "2px", :width "100%", :background-color "grey"}}]
   [:div nil]])

(defn body []
  [:body  {:style {:margin "auto"}
           :data-spy "scroll"
           :data-target "#toc"}
   [:div.container
    [:div.row
     [:div {:class "col-sm-12"}
      [:div
       [:f> my-component]]]]]
   #_[:script {:type "text/javascript"}
      (-> "highlight/highlight.min.js"
          io/resource
          slurp)]
   #_[:script {:type "text/javascript"}
      "hljs.highlightAll();"]])

(rdom/render [body] (.getElementById js/document "app"))