# Arisia Backend

The backend is written in the [Scala 2.13](https://docs.scala-lang.org/)
programming language. It is built on top of the
[Play Framework](https://www.playframework.com/documentation/2.8.x/ScalaHome),
a high-performance application framework with a built-in web
server.

The attendee-facing user interface is written in Angular, and
can be found in the `frontend` directory parallel to this one.
The backend proxies that out, but for the most part the front
end code is a black box as far as the backend is concerned. The
primary responsibility of the backend is to provide RESTful
APIs that the frontend can call, and to deal with the behind-the-scenes
data management.

The backend does provide its own UI (still to be started, as
of this writing), specifically for Admin use only. That is
written in [Twirl](https://www.playframework.com/documentation/2.8.x/ScalaTemplates),
which is Play's built-in template engine -- aside from the fact
that the embedded code is Scala, it looks pretty much the same
as Rails, Grails, Razor, PHP, etc.

## Contributing to the Backend code

If you're thinking of helping out on the Backend side: howdy!

There are a number of styles of Scala code; this repo is
generally following "Lightbend style" -- not hardcore functional
programming, but following Scala best practices pretty strictly.
The key rules to keep in mind, if you aren't used to Scala, are:

* **No nulls:** `null` should only be used where strictly necessary
  (basically, when directly interfacing with Java libraries,
  which we probably won't do anyway). If you are thinking of
  using `null`, use `Option[T]` instead.
* **No thread blocking:** Play achieves high performance by being
  scrupulous about async-everywhere. Blocking is only permitted in
  dedicated thread pools; in practice, that should be limited to
  the internals of the libraries we are using. (Exception:
  blocking is permitted in test code, or temporary stubs.) In
  general, if you are tempted to block, you should probably
  be using `Future[T]` instead.
* **No static state:** the Scala equivalent of Java `static`
  members is `var`s placed in `object`s. Don't do that for any
  potentially mutable state: it's fine for constants, but you
  should never do it for anything else. All common state belongs
  in the top-level Service classes.
* **Avoid mutability:** while we're not going full-on FP-style
  No Mutability Period, we are constraining it to safe
  `AtomicReference`s in the top-level service objects. There
  should be no need for it elsewhere. Top-level service objects
  are allowed to have `var`s that are set *once*, during startup;
  there should be no `var`s anywhere else outside of test code.
* **No locks:** the equivalent of a Java lock is the Scala
  `synchronized` construct. Don't use it -- if you
  have a problem that can't be handled with an `AtomicReference`,
  that indicates a design problem that needs to be rethought.
* **Separation of Interface and Implementation:** this one is less
  standard-style, and more because it's necessary in order to have
  any hope of serious automated tests. Most of the logic of the
  system is in the top-level Service objects. In all cases, you
  should expose the Service as an abstract trait (an interface),
  and implement it as an Impl subclass of that trait. It's a little
  extra boilerplate, but makes it vastly easier to set things up
  to test.
  
Talk to Justin if you have questions about any of these, or need
examples of how to deal with them.
  
A common theme underlying all of this is that this is a massively
multi-threaded server. You *must* assume that there are multiple
threads happening at a given time. We are going to strictly avoid
playing games with fine-grained locks, because it is extremely
hard to do that correctly, avoiding both race conditions and
deadlocks. Just Don't Go There.