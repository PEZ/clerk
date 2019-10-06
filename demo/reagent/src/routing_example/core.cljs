(ns routing-example.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [bidi.bidi :as bidi]
            [accountant.core :as accountant]
            [clerk.core :as clerk]))

(enable-console-print!)

(def app-routes
  ["/" {"" :index
        "a-items" {"" :a-items
                   ["/item-" :item-id] :a-item}
        "b-items" {"" :b-items
                   ["/item-" :item-id] :b-item}
        "about" :about
        "missing-route" :missing-route
        true :four-o-four}])

(session/put! :demo-using-clerk? true)

(defn clerk-toggler []
  [:input {:id :demo-using-clerk?
           :type "checkbox"
           :checked (session/get :demo-using-clerk?)
           :on-change (fn [e]
                        (session/put! :demo-using-clerk? (not (session/get :demo-using-clerk?))))}])

(defmulti page-contents identity)

(defmethod page-contents :index []
  (fn []
    [:span.main
     [:h1 "A silly Clerk Demo"]
     [:p [:a {:href "https://github.com/PEZ/clerk"} "Clerk"] " is a ClojureScript library designed to make it easy to get your Single Page Application to behave more like a ”regular” site would do when it comes to navigating between, and within, pages."]
     [:div#try-this-at-home-kids
      [:h2 "Things you can try:"]
      [:ol
       [:li "Disable Clerk --> " [:label [clerk-toggler] "Use Clerk?"]]
       [:li "Visit " [:a {:href (bidi/path-for app-routes :a-items)} "lots of items of type A"]]
       [:li "Scroll down until you find a link to item " [:strong "50"] " of B. Note which item is at the top of the screen."]
       [:li "Scroll to somewhere else on the page."]
       [:li "Use the the browser's Back and Forward buttons to see if scroll positions are restored correctly."]
       [:li "Click the " [:strong "Go to bottom/top"] " links in the header and footer of those long pages"]]
      [:p "Now do the above things with Clerk enabled. 😀"]]
     [:h2 "Some more links."]
     [:div
      [:ul
       [:li [:a {:href (bidi/path-for app-routes :b-items)} "Many items of type B"]]
       [:li [:a {:href (bidi/path-for app-routes :missing-route)} "A Missing Route"] " (This isn't Clerk related, but rather a Bidi thing.)"]
       [:li [:a {:href "/borken/link"} "A Borken Link"] " (Again, Bidi related.)"]]]
     [:div
      [:p "Using "
       [:a {:href "https://reagent-project.github.io/"} "Reagent"] ", "
       [:a {:href "https://github.com/juxt/bidi"} "Bidi"] ", "
       [:a {:href "https://github.com/venantius/accountant"} "Accountant"] " & "
       [:a {:href "https://github.com/PEZ/clerk"} "Clerk"]
       ". Want to start off your next project using these libraries? Take a look at " [:a {:href "https://github.com/PEZ/reagent-bidi-accountant-example"} "Github"]]
      [:div
       [:p "For developing Clerk, I have made good use of " [:a {:href "https://browserstack.com"} "Browserstack"]]
       [:a {:href "https://browserstack.com"}
        [:img {:src "images/Browserstack-logo.svg"
               :width "240px"}]]]]]))


(defmethod page-contents :a-items []
  (fn []
    [:span.main
     [:h1 "The Lot of A Items"]
     [:div#red {:style {:width "50%" :height "200px" :background-color "red"}}]
     [:ul
      (map (fn [item-id]
             [:li {:id (str "item-" item-id) :key (str "item-" item-id)}
              [:a {:href (bidi/path-for app-routes :item :item-id item-id)} "A-item: " item-id]])
           (range 1 42))]
     [:div {:style {:width "50%" :height "200px" :background-color "green"}}]
     [:div#b-item-100-link [:a {:href (str (bidi/path-for app-routes :b-items) "#item-50")} "B-item: 50"]]
     [:div {:style {:width "50%" :height "200px" :background-color "blue"}}]
     [:ul
      (map (fn [item-id]
             [:li {:id (str "item-" item-id) :key (str "item-" item-id)}
              [:a {:href (bidi/path-for app-routes :item :item-id item-id)} "A-item: " item-id]])
           (range 42 78))]
     [:div {:style {:width "50%" :height "200px" :background-color "yellow"}}]
     [:p [:a {:href (bidi/path-for app-routes :b-items)} "Top of b-items"]]]))


(defmethod page-contents :b-items []
  (fn []
    [:span.main
     [:h1 "The Many B Items"]
     [:ul (map (fn [item-id]
                 [:li {:id (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (bidi/path-for app-routes :item :item-id item-id)} "B-item: " item-id]])
               (range 1 117))]
     [:p [:a {:href (bidi/path-for app-routes :a-items)} "Top of a-items"]]]))


(defmethod page-contents :a-item []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of A")]
       [:p [:a {:href (bidi/path-for app-routes :items)} "Back to the list of A-items"]]])))


(defmethod page-contents :b-item []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of B")]
       [:p [:a {:href (bidi/path-for app-routes :items)} "Back to the list of B-items"]]])))


(defmethod page-contents :about []
  (fn [] [:span.main
          [:h1 "About routing-example"]]))


(defmethod page-contents :four-o-four []
  "Non-existing routes go here"
  (fn []
    [:span.main
     [:h1 "404: It is not here"]
     [:pre.verse
      "What you are looking for,
I do not have.
How could I have,
what does not exist?"]]))


(defmethod page-contents :default []
  "Configured routes, missing an implementation, go here"
  (fn []
    [:span.main
     [:h1 "404: My bad"]
     [:pre.verse
      "This page should be here,
but it is not."]]))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (bidi/path-for app-routes :index)} "Go home"] " | "
         [:a {:href (bidi/path-for app-routes :about)} "See about"] " | "
         [:a {:href "#bottom"} "Bottom of page"] " | "
         [:label [clerk-toggler] " Use Clerk?"]]]
       ^{:key page} [page-contents page]
       [:footer#bottom
        [:p [:a {:href (bidi/path-for app-routes :index)} "Go home"] " | "
         [:a {:href (bidi/path-for app-routes :about)} "See about"] " | "
         [:a {:href "#top"} "Top of page"]]]])))

(defn on-js-reload []
  (reagent/render-component [current-page]
                            (. js/document (getElementById "app"))))

(defn ^:export init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler (fn
                   [path]
                   (when (session/get :demo-using-clerk?)
                     (reagent/after-render clerk/after-render!))
                   (let [match (bidi/match-route app-routes path)
                         current-page (:handler match)
                         route-params (:route-params match)]
                     (session/put! :route {:current-page current-page
                                           :route-params route-params}))
                   (when (session/get :demo-using-clerk?)
                     (clerk/navigate-page! path)))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!)
  (on-js-reload))
