(ns routing-example.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]

            [bidi.bidi :as bidi]
            ;;[schema.core :as s] ;For when defining routes get tricky
            ;;[bidi.schema]

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

;;(s/check bidi.schema/RoutePair app-routes)
;;(s/validate bidi.schema/RoutePair app-routes)

(defmulti page-contents identity)

(defmethod page-contents :index []
  (fn []
    [:span.main
     [:h1 "Welcome to routing-example"]
     [:ul
      [:li [:a {:href (bidi/path-for app-routes :a-items)} "Lots of items of type A"]]
      [:li [:a {:href (bidi/path-for app-routes :b-items)} "Many items of type B"]]
      [:li [:a {:href (bidi/path-for app-routes :missing-route)} "A Missing Route"]]
      [:li [:a {:href "/borken/link"} "A Borken Link"]]]
     [:p "Using "
      [:a {:href "https://reagent-project.github.io/"} "Reagent"] ", "
      [:a {:href "https://github.com/juxt/bidi"} "Bidi"] ", "
      [:a {:href "https://github.com/venantius/accountant"} "Accountant"] " & "
      [:a {:href "https://github.com/PEZ/clerk"} "Clerk"]
      ". Find this example on " [:a {:href "https://github.com/PEZ/reagent-bidi-accountant-example"} "Github"]]]))


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
        [:p#top [:a {:href (bidi/path-for app-routes :index)} "Go home"] " | "
         [:a {:href (bidi/path-for app-routes :about)} "See about"] " | "
         [:a {:href "#bottom"} "Bottom of page"] " | "
         [:input {:id :use-clerk?
                  :type "checkbox"
                  :checked (session/get :use-clerk?)
                  :on-change (fn [e]
                               (session/put! :use-clerk? (not (session/get :use-clerk?))))}]
         [:label {:for :use-clerk?} "Use Clerk?"]]]
       ^{:key page} [page-contents page]
       [:footer
        [:p#bottom [:a {:href (bidi/path-for app-routes :index)} "Go home"] " | "
         [:a {:href (bidi/path-for app-routes :about)} "See about"] " | "
         [:a {:href "#top"} "Top of page"]]]])))

(defn on-js-reload []
  (reagent/render-component [current-page]
                            (. js/document (getElementById "app"))))

(defn ^:export init! []
  (session/put! :use-clerk? true)
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler (fn
                   [path]
                   (when (session/get :use-clerk?)
                     (reagent/after-render clerk/after-render!))
                   (let [match (bidi/match-route app-routes path)
                         current-page (:handler match)
                         route-params (:route-params match)]
                     (session/put! :route {:current-page current-page
                                           :route-params route-params}))
                   (when (session/get :use-clerk?)
                     (clerk/navigate-page! path)))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!)
  (on-js-reload))
