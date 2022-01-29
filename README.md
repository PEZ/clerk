# clerk

A ClojureScript library designed to make it easy to get your Single Page Application to behave more like a ”regular” site would do when it comes to navigating between, and within, pages.

Online demo here: [clerk-demo.netlify.com](https://clerk-demo.netlify.com)

[![Clojars Project](https://img.shields.io/clojars/v/pez/clerk.svg)](https://clojars.org/pez/clerk)

Clerk takes care of the scroll positioning when:
* Navigating to a new page, e.g. if the user clicks a link to another page.
  * Scroll is set to the top of the page in these cases.
* Navigation to target anchors within the page.
  * Scroll is smoothly adjusted to the top of the target element.
  * _This only works if the routing library you are using supports hash targets._ [Secretary](https://github.com/gf3/secretary) doesn't really. But [Bidi](https://github.com/juxt/bidi) does.
* Navigating back/forth using the web browser history navigation.
  * Scroll position is restored to whatever it was when the user left it.

Clerk does not deal with anything else beside the above. Use it together wih your routing and HTML5 history libararies of choice.

## The Problem
Today's web browsers handle all this automatic scroll positioning perfectly for regular sites. But the S in SPA really means that everything happens on the same page, even if it looks to the user as if navigatin between pages happens. A new page is just the result of rendering new content. So without managing the scroll positioning we have this UX problem:

<a href="Without Clerk.png"><img alt="Without Clerk" src="Without Clerk.png" width="100%"/></a>

In addition to this:

* The browser's default scroll restoration for history navigation can't be trusted within an SPA. It sometimes looks like it works, but then comes with big time surprises at other times.
* In-page navigation to anchor targets doesn't happen at all unless we add code for it.

Let Clerk take care of all this for you!

## Usage

A super easy way to try out Clerk in a new project is to use the [Leingen Reagent Template](https://github.com/reagent-project/reagent-template):

```bash
$ lein new reagent <project-name>
```

For other scenarious, read on.

### Setup

Add the dependency:
```clojure
[pez/clerk "1.0.0"]
```

For the instructions below, I will assume Clerk is required like so:
```clojure
(:require
 ...
 [clerk.core :as clerk]
 ...
```

### Initialize
Initialize Clerk as early as possible when your app is starting:
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

Depending on your project the after render notification will need to be injected in different ways. Here are exemples for two common ClojureScript React frameworks, [Rum](https://github.com/tonsky/rum) and [Reagent](http://reagent-project.github.io):

#### Rum
Rum has a utility callback `:after-render` that can be used in a mixin for this purpose, like so:
```clojure
(defc page < rum/reactive
  {:after-render
   (fn [state]
     (after-render!)
     state)})
  ...
```

#### Reagent
For Reagent, you can use the `reagent/after-render` function, which calls any function you provide to it when rendering is done:
```clojure
(reagent/after-render clerk/after-render!)
```

(You can also hook it in to the compenent life cycle, `:component-did-mount` and `:component-did-update`, if that suits your project and taste better.)

### Putting it together
The Leiningen [Reagent template](https://github.com/reagent-project/reagent-template)'s `init!` function will look like so with all clerky stuff added:
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

### It is not Always this Simple
Registering `clerk/after-render!` on the ”page component” or on navigation dispatch is not sufficient for some applications. Some pages get loaded and rendered in phases and for some apps it can take quite a while before they have all the data they need to render the final page. (And lots of other cases.) Finding the right entry point to inject the Clerk functions/commands will sometimes be a challenge. I am very interested to hear about challanges and solutions!


## Caveats

* **IMPORTANT**: If you are using some kind of analytics (like Google Analytics) for stats on site usage for your SPA, take care with any history change events resulting in ”virtual” page hits. Clerk uses the browser's history state to store the current scroll position for the page. Specifically, *the default History Change Trigger of Google Tag Manager can't be used as is*. You risk spamming your stats with ”page views” that really are just the user scrolling.
* Clerk depeds on HTML 5 history and does not handle routing that rely on prefixing route paths with '#'. If you still need to target browsers that do not have HTML 5 history: no Clerk for you.

## What About the Name?

*In Scotland, the term scrow was used from about the 13th to the 17th centuries for scroll, writing, or documents in list or schedule form. There existed an office of Clerk of the Scrow (Rotulorum Clericus) meaning the Clerk of the Rolls or Clerk of the Register.* (From [Wikipedia](https://en.wikipedia.org/wiki/Scroll#Scotland).)

Also, it is quite beautiful to imagine that with some projects maybe [Secretary](https://github.com/gf3/secretary), [Accountant](https://github.com/venantius/accountant) and Clerk will work together to get the SPA to behave according to the expectations of its users.

## Happy Coding ❤️ Feedback Welcome

Questions, suggestions, PRs. Just throw it at me. File issues at will. You can also most often find me at the [Clojurians Slack](https://clojurians.slack.com). Have praise? Tweet it! Tag [@pappapez](https://twitter.com/pappapez).

## License

Copyright © 2018 Peter Strömberg

Distributed under the Eclipse Public License, either version 1.0 or (at your option) any later version.

## Tested with Browserstack

I'm pretty confident about that Clerk works in reasonably modern web browsers, much thanks to [BrowserStack](https://browserStack.com).

<a href="https://browserStack.com"><img src="resources/Browserstack-logo.svg" width="240px"/></a>
