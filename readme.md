Clay is a library that can turn a Clojure namespace into Hiccup (HTML) or Markdown.
I want to extend clay to turn clojure code into reagent components that can be rendered using scittle, or any react environment.

run `bb serve` to start a demo server, open http://localhost:1337/ in browser.
`main.clj` contains rich comments show how to use this.

## How it works
In the notebook/items-and-test-forms function, Clay will first read Clojure code. 
Each top-level form will be converted to a note, as in notes in a notebook.

And then evaluate each note, and turn the note into an `item`. An `item` is just a map with the origin source code and evaluated result.

And the item will be passed to prepare namespace. The prepare function would convert the item to Hiccup or Markdown, based on the value type. 
Some values are more suitable to be rendered as Markdown, and some are more suitable to be rendered as Hiccup. Sometimes there will be both.

After the prepare function, the item will contain a :hiccup or :markdown key, or both.

I first ran all the examples in Kindly's documentation, to see what every kind of item looks like.

For Clojure comments `;; some comment`, the item contains :md
```clojure
{:md "some comment"}
```
To render it in Hiccup, I just wrap it in a `<p>` tag.

For basic values, like numbers, strings, booleans, and Clojure data structures, lists and maps etc., they all contain a :hiccup field in the item. And the Hiccup form is static.
It can be directly dropped into a Reagent render function.

For example, the item for `1`
```clojure
{:printed-clojure true
 :hiccup [:div
          [:pre [:code.sourceCode.language-clojure.printed-clojure "1\r\n"]]]
 :md "\n::: {.printedClojure}\n```clojure\n1\r\n\n```\n:::\n"
 :kindly/options
 {:datatables
  {:paging false :scrollY 400 :sPaginationType "full_numbers" :order []}}}
```

Just take the :hiccup field and it is a Reagent component.
There are some CSS classes, will handle later.

For strings, it is a little tricky. Hiccup will escape `"str"` to `"&quot;str&quot;"`.
If it renders directly in Reagent, it will show `&quot;str&quot;`.

```clojure
{:source-clojure true
 :hiccup [:div
          [:pre
           [:code.sourceCode.language-clojure.source-clojure.bg-light
            "&quot;str&quot;"]]]
 :md     "\n::: {.sourceClojure}\n```clojure\n\"str\"\n```\n:::\n"}
```

For this case, I need to use the `dangerouslySetInnerHTML` attribute in Reagent.
The original Hiccup needs to become 
```clojure
[:div
  [:pre
    [:code
      {:class "sourceCode language-clojure source-clojure bg-light"
      :dangerouslySetInnerHTML {:__html "&quot;str&quot;"}}]]]
```

I use clojure.walk/postwalk to walk the Hiccup, find any "code" tag and wrap it with 
`dangerouslySetInnerHTML`. Maybe some other tags also need to do this? Not sure now.

Many other kinds like Reagent, ECharts, KaTeX, they all contain script tags in Hiccup.

For example, KaTeX looks like this:
```clojure
{:md "$$x^2=\\alpha$$"
 :hiccup
 [:div
  [:div
   [:script
    "katex.render(\"x^2=\\\\alpha\", document.currentScript.parentElement, {throwOnError: false});"]]]
 :deps [:katex]
 :kindly/options
 {:datatables
  {:paging false :scrollY 400 :sPaginationType "full_numbers" :order []}}}
```

It uses the `katex` object in the global JS scope, so Clay also includes a :deps field, to record what JS library is used. Later when converting to HTML, it will include all the JS libs in the HTML head.

I just assume the library is in the global JS scope. But there is another problem.
> React DOM (the renderer for React on web) uses createElement calls to render JSX into DOM elements.

createElement uses the innerHTML DOM API to finally add these to the DOM (see code in React source). innerHTML does not execute script tags added as a security consideration. And this is the reason why in turn rendering script tags in React doesn't work as expected.
https://stackoverflow.com/a/64815699/8388083

To solve this, I use a React useEffect hook to create a script tag, and add it back to the DOM.
```clojure
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
```

Also create a wrapper to use this hook.
```clojure
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
```

So the original Hiccup form with script needs to change from
```clojure
[:div
   [:script
    "katex.render(\"x^2=\\\\alpha\", document.currentScript.parentElement, {throwOnError: false});"]]
```

to
```clojure
[wrap-scripts
    {:scripts
      [[:script
        "katex.render(\"x^2=\\\\alpha\", document.currentScript.parentElement, {throwOnError: false});"]]}
    ["div" {}]]
```

I also use clojure.walk for this transformation.

## TODO
- This should be a render option like :html and :md in Clay or a separate library maybe `kindly-reagent`
- Clay will write some CSV files to temp directory, for example when the vega/vega-lite data is given in CSV. Kindly didn't specify how to handle this.
- Kindly also didn't specify CSS?