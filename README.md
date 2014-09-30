# logic

A hands-on introduction to [core.logic](https://github.com/clojure/core.logic) for
Cambridge Clojure User Group.

This tutorial uses examples from [The Reasoned Schemer](http://mitpress.mit.edu/books/reasoned-schemer)
and [Prolog Programming for Artificial Intelligence](http://books.google.co.uk/books/about/Prolog_Programming_for_Artificial_Intell.html?id=-15su78YRj8C). These are both good resources if you want to learn more. The following online resources are also very good:

* [core.logic on Github](https://github.com/clojure/core.logic)
* [David Nolen's core.logic tutorial](https://github.com/swannodette/logic-tutorial)
* [The Magical Island of Kanren](http://objectcommando.com/blog/2011/11/04/the-magical-island-of-kanren-core-logic-intro-part-1/)

## Usage

If you want to follow along with the examples in the REPL, first [install Leiningen](http://leiningen.org/#install) then:

    git clone https://github.com/cam-clj/logic.git
    cd logic
    lein deps
    lein repl

Of course, you can start a REPL from your favourite editor if you
prefer.

If you don't have `git` installed, then you can download a zip
archive instead:

    wget https://github.com/cam-clj/logic/archive/master.zip

We suggest you work through the examples in the following order:

* basics.clj
* family.clj
* lists.clj
* flight_plan.clj
* n_queens.clj
* sudoku.clj

## License

Copyright © 2014 Ray Miller <ray@1729.org.uk>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
