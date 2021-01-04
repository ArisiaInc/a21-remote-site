# a21-remote-site
Website for logged-in attendees of Arisia 2021

## Setting up a Local Build

There are two subdirectories under here, `arisia-remote` (the
backend) and `frontend`. To build and run the system locally,
here are the steps.

### Install Postgres

You will need a Postgres database in order to run the system.
It doesn't matter how you install it or where it lives, but
you need to know the JDBC connection URL to get to it.

### Install the Front End Tools

See [the frontend README](frontend/README.md), specifically the
"Getting Started" section. That describes how to install the
underlying pieces that you need in order to build the frontend.

### Install the Back End Tools

#### Install git

If git isn't already available, install that, so that you can
get this code.

#### Clone this repo

If you don't already have this code, use `git clone` to fetch it.

#### Install the JVM

If there isn't already a JVM available, install one. Note that
Scala requires an older JVM: JVM 11 is probably best, and JVM 8
is fine. Newer JVMs are not as well-tested in the Scala world,
so there is a bit of risk if you use a new one.

#### Install coursier

[coursier](https://get-coursier.io/docs/cli-installation) is a
relatively new dependency-management tool used for Scala
development. It deals with much of the rest of this, simplifying
a process that used to be a headache.

#### Install the Scala Stack

[Run the setup task in coursier](https://get-coursier.io/docs/cli-setup).
The only thing you *really* need to install is sbt, but it's
fine to install of the components.

#### Build the System

The backend is built in Scala, using the Play Framework. In the
Scala world, the really important tool is `sbt`, the Scala Build
Tool. This is the equivalent of make/maven/gradle/bazel/pants/etc:
it takes a `build.sbt` file that describes the system, and
deals with building, testing and (sometimes) running it.

Run `sbt` in this directory. That will read in the `build.sbt`
file, and use that to understand how the application is
constructed. It will put you into the sbt shell. (You can
exit this shell with ctrl-d.)

`sbt` provides many commands, but only a few are essential. The
`compile` command compiles *just the backend*. We have added a
`build` command that compiles both the backend and frontend.
(This is still pretty primitive, and likely to evolve, but is
basically working.)

#### Set up secrets.conf

In the [arisia-remote/conf](arisia-remote/conf) directory, you
will find a file named `secrets.conf.template`. Copy that to
`secrets.conf`. This is where non-public, installation-specific
stuff lives. You are **not allowed to check secrets.conf in!**
That's important: it's full of secrets, and must not be checked
into GitHub!

##### Create an Application Secret

The Application Secret is used to encrypt the session cookie.
To create one of your own, go into `sbt`, and say
`playGenerateSecret`. That will give you a long randomized
string. In your `secrets.conf` file, put this string in the
`play.http.secret.key` setting.

###### Set the Postgres Connection URL

Put the JDBC URL to connect to your development database into
`secrets.conf`, in the `db.default.url` setting. (In a dev
environment this may not contain any secrets, but it is not
unusual for it to contain an embedded password, so it is
generally considered a secret.)

###### Alternatively to installing postgres, use the docker file

You need to already have docker (and docker-compose, which comes with it)
on your machine. Then you can run `docker-compose up -d` and it will
spawn a postgres server running on localhost:5432 for you. The connection
string for this db is commented out in the `secrets.conf.template` file.

Note: this will not work if you have something running at 5432 already.

#### Running the System

At the moment, the backend and frontend are not connected to
each other. (This will change soon.)

To run the frontend, see [the frontend README](frontend/README.md).
That will serve a basic page on localhost:4200.

To run the backend, in the sbt shell, say `backend/run`. This will boot
the backend server, and provide a basic "hello world" page on
localhost:9000.

## Structural Credits

The way the front and back ends are hooked together is largely
adapted from
[this article on integrating Play and Angular](https://torre.me.uk/2019/03/06/scala-play-rest-and-angular/).
