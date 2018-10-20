# clerk

A ClojureScript library designed to make it easy to get your Single Page Application to behave more like a ”regular” site would do when it comes to navigating between, and within, pages.

[![Clojars Project](https://img.shields.io/clojars/v/pez/clerk.svg)](https://clojars.org/pez/clerk)

Clerk takes care of the scroll positioning when:
* Navigating to a new page, e.g. if the user clicks a link to another page.
  * Scroll is set to the top of the page in these cases.
* Navigation to target anchors within the page.
  * Scroll is smoothly adjusted to the top of the target element.
  * _This only works if the routing library you are using supports hash targets._ Secretary doesn't really. But Bidi does.
* Navigating back/forth using the web browser history navigation.
  * Scroll position is restored to whatever it was when the user left it.

Clerk does not deal with anything else beside of the above. Use it together wih your routing and HTML5 history libararies of choice.

## The Problem
Today's web browsers handles all this automatic scroll positioniing perfectly for regular sites. But the S in SPA really means that everything happens on the same page, even if it looks to the user as if navigatin between pages happens. A new page is just the result of rendering new content. So without managing the scroll positioning we have this Ux problem:

<a href="Without Clerk.png"><img alt="Without Clerk" src="Without Clerk.png" width="100%"/></a>

In addition to this:

* The browser's default scroll restoration for history navigation can't be trusted within an SPA. It sometimes looks like it works, but then comes with big time surprises at other times.
* In-page mavigation to anchor targets doesn't happen at all unless we add code for it.

Let Clerk take care of all this for you!

## Usage

Add the dependency:
```clojure
[pez/clerk "0.1.0-SNAPSHOT"]
```

The examples in this README assumes Clerk is required like so:
```clojure
(:require
 ...
 [clerk.core :as clerk]
 ...
```

### Initialize
Initialize as early as possible when your app is starting:
```clojure
(clerk/initialize!)
```

### After Navigation Dispatch
After any routing/navigation dispatch of your app you need to tell Clerk about the new path
```clojure
(clerk/navigate-page! path)
```

### After Render
Then just one more thing. To avoid flicker, Clerk deferrs scroll adjustment until after the page is rendered. You need to tell Clerk when rendering is done:
```clojure
(clerk/after-render!)
```

Depending on your project the after render notification will need to be injected in different ways. Here are exemples for two common ClojureScript React frameworks, Rum and Reagent:

#### Rum
Clerk has a utility [Rum](https://github.com/tonsky/rum) mixin for after-render. Use like so:
```clojure
(defc page < rum/reactive
             clerk/rum-after-render
  ...
```

#### Reagent
For [Reagent](http://reagent-project.github.io), you can use the `reagent/after-render` function, which calls any function you provide to it when rendering is done:
```clojure
(reagent/after-render clerk/after-render!)
```

### Putting it together
The Leiningen [Reagent template](https://github.com/reagent-project/reagent-template)'s `init!` function will look lie so with all clerky stuff added:
```clojure
(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (reagent/after-render clerk/after-render!)
      (secretary/dispatch! path)
      (clerk/navigate-page! path))
    :path-exists?
    (fn [path]
      (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
```

(For Rum it will look very similar, except that you need to use a mixin for your page component's `:after-render`callback instead of using the `reagent/after-render`.)

## Caveats

**IMPORTANT**: If you are using some kind of analytics (like Google Analytics) for stats on site usage for your SPA, take care with any history change events resulting in ”virtual” page hits. Clerk uses the browser's history state to store the current scroll position for the page. Specifically, *the default History Change Trigger of Google Tag Manager can't be used as is*. You risk spamming your stats with ”page views” that really are just the user scrolling.


## What About the Name?

*In Scotland, the term scrow was used from about the 13th to the 17th centuries for scroll, writing, or documents in list or schedule form. There existed an office of Clerk of the Scrow (Rotulorum Clericus) meaning the Clerk of the Rolls or Clerk of the Register.* (From [WikiPedia](https://en.wikipedia.org/wiki/Scroll#Scotland).)

Also, it is quite beautiful to imagine that with some projects maybe [Secretary](https://github.com/gf3/secretary), [Accountant](https://github.com/venantius/accountant) and Clerk will work together to get the SPA to behave according to the expectations of its users.

## Feedback Welcome

Questions, suggestions, PRs. Just throw it at me. File issues at will. You can also most often find me at the [Clojurians Slack](https://clojurians.slack.com). Have praise? Tweet it! Tag [@pappapez](https://twitter.com/pappapez).

## License

Copyright © 2018 Peter Strömberg

Distributed under the Eclipse Public License, either version 1.0 or (at your option) any later version.
