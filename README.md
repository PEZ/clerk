# clerk

A ClojureScript library designed to make it easy to get your Single Page Application to behave more like a ”regular” site would do when it comes to navigating between, and within, pages.

## Usage

Add the dependency:
```clojure
[pez/clerk "0.1.0"]
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

### After navigation dispatch
After any routing/navigation dispatch of your app you need to tell Clerk about the new path
```clojure
(clerk/navigate-page! path)
```

### After render
Then just one more thing. To avoid flicker, Clerk deferrs scroll adjustment until after the page is rendered. You need to tell Clerk when rendering is done:
```clojure
(clerk/after-render!)
```

Depending on your project the after render notification will need to be injected in different ways. Here are exemples for two common ClojureScript React frameworks, Rum and Reagent:

#### Rum
Clerk has a utility Rum mixin for after-render. Use like so:
```clojure
(defc page < rum/reactive
             clerk/rum-after-render
  ...
```

#### Reagent
For Reagent, you can use the `reagent/after-render` function, which calls any function you provide to it when rendering is done:
```clojure
(reagent/after-render clerk/after-render!)
```

### Putting it together
The Leiningen Reagent template's `init!` function will look lie so with all clerky stuff added:
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

## Caveats

Analytics, tag manager, history change

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
