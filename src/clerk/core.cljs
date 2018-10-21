(ns ^{:doc "Clerk: In-page navigation (scrolling) for SPAs.
  Tries to mimic the default browser behaviour for ”regular” sites.
  Use Clerk together with your router and HTML5 history manager of choice.
            
  New navigxation history entries are always scrolled to the hash target in the location,
  or, when the target is missing, the top of the page.
  When the user is navigating to existing history entries we restore the scroll position.
  The reason for this is that the browser's own restoration is not timed with the rendering
  of the page and this crates a 'jumpy' experience. Unfortunately only Chrome supports a
  reliable way to disable the browser's scroll restoration, but at least there we can create a
  smooth scroll restoration experience."}
 clerk.core
  (:require
   [goog.events :as events]
   [goog.events.EventType :as EventType]
   [goog.fx.dom :as fx-dom]
   [goog.dom :as dom]
   [goog.style :as style]
   [cljs.core.async :as a])
  (:import goog.Uri))


(def ^:private current-path (atom (.-pathname js/location)))


(def ^:private deferred-navigation-chan (a/chan (a/sliding-buffer 1)))


(defn- debounce
  ([f] (debounce f 1000))
  ([f timeout]
   (let [id (atom nil)]
     (fn [evt]
       (if (not (nil? @id))
         (js/clearTimeout @id))
       (reset! id (js/setTimeout
                   (partial f evt)
                   timeout))))))


(defn- defer-page-navigation!
  "We put any navigation that needs to be performed after the page is rendered here,
   in the form of a function to be called."
  [page-nav-fn]
  (a/offer! deferred-navigation-chan page-nav-fn))


(defn- get-history-state
  "Where we save stuff we want to remember when the user
  navigates back and forths in the history stack"
  []
  (.-state js/history))


(defn- get-history-scroll-top
  "If we have a saved scroll-top in the history state return it,
   otherwise return nil"
  []
  (when-let [state (get-history-state)]
    (aget state "scroll-top")))


(defn- get-scroll-top
  "Current scroll position"
  []
  (.-y (dom/getDocumentScroll)))


(defn- scroll-to
  "Scroll immediatelly to y"
  [y]
  (js/scrollTo 0 y))


(defn- smooth-scroll-to
  "Scroll with some easing"
  [y]
  (.play (fx-dom/Scroll. (dom/getDocumentScrollElement) (clj->js [0 (get-scroll-top)]) (clj->js [0 y]) 300)))


(defn- top-of-element-with-id [id]
  (if-let [element (dom/getElement id)]
    (style/getPageOffsetTop element)
    0))


(defn- bottom-of-element-with-id [id]
  (if-let [element (dom/getElement id)]
    (+ (.-y (style/getPosition element)) (.-height (style/getSize element)))
    0))


(def ^:private browser-supports-manual-restoration? (.-scrollRestoration js/history))


(defn- disable-default-scroll-restoration
  "The browser's default behaviour is to restore scroll position on revisit of a
  history stack entry. Chrome supports to disable it. Other browsers don't really,
  but there's no visible penalty for trying, so we try."
  []
  (if browser-supports-manual-restoration?
    (set! (.-scrollRestoration js/history) "manual")
    (events/listen
     js/window EventType/POPSTATE
     (fn [event]
       (if-let [state (.-state event)]
         (when-let [scroll-top (aget state "scroll-top")]
           (events/listenOnce
            js/window EventType/SCROLL
            (fn []
              (js/scrollTo 0 scroll-top)
              nil))))
       nil))))


(defn- install-scroll-saver
  "When the user scrolls we save the current scroll position in the
  history objects state."
  []
  (events/listen
   js/window EventType/SCROLL
   (debounce
    (fn [event]
      (let [scroll-top (get-scroll-top)
            state {:scroll-top scroll-top}]
        (.replaceState js/history (clj->js state) (.-title js/document)))
      nil)
    200)))


(defn after-render!
  "Call this after the page has rendered to perform any deferred
   page-navigation (i.e. scrolling)."
  []
  (when-let [page-nav-fn (a/poll! deferred-navigation-chan)]
    (page-nav-fn)))


(defn navigate-page!
  "Call this on every navigation.
   Navigates the page by scrolling to the appropriate y position. It's either:
   * A history stack navigation => any saved scroll position should be used
   * A new history stack element => either scroll to the top or to any hash target (URL fragment)
   In both cases consider if the navigation is:
   * on the same page -> scroll immediatelly (and smooth)
   * not on the same page -> defer scrolling (for an after render handler to deal with
   If a `top-element-id` is given we translate the y coordinate to the bottom
   the top element (sometimes needed with those sticky menus at the top of pages.)"
  [url & [top-element-id]]
  (assert (string? url))
  (let [uri (.parse Uri url)
        path (.getPath uri)
        old-path (.getPath (.parse Uri @current-path))
        same-page? (= path old-path)
        fragment (not-empty (.getFragment uri))]
    (reset! current-path path)
    (if-let [y (get-history-scroll-top)]
      (if same-page?
        (smooth-scroll-to y)
        (defer-page-navigation! #(scroll-to y)))
      (let [y-translation (if top-element-id (bottom-of-element-with-id top-element-id) 0)]
        (if (and same-page? fragment)
          (let [y (top-of-element-with-id fragment)]
            (smooth-scroll-to (- y y-translation)))
          (defer-page-navigation! (fn []
                                    (let [y (if fragment
                                              (top-of-element-with-id fragment)
                                              0)]
                                      (scroll-to (- y y-translation))))))))))


(defn initialize!
  "Call when your app is starting."
  []
  (disable-default-scroll-restoration)
  (install-scroll-saver))
