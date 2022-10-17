# spj - json validation tech test

Tech interview take home test.

Elided adding link or explicit reference to assignment so that people can't just search for the answer on github. I
guess.

## FAQ

Q: Is this server over-engineered for what it needs to do!?
A: Absolutely! But then again, it's an interview test. Therefore, I wanted to show some useful patterns that help make
wrangling
the complexity of a ten-fold larger codebase manageable.

Q: why don't I see a `import cats.effect.*`?
A: check `modules/spj-prelude/src/main/scala/spj/package.scala`. It exports everything we deem to be "standard library".
Idem for other modules as well :)

## Scala version

Project written in Scala 3. Tooling has a long way to go, it's really a bother. It's definitely more productive to write
in Scala 2 in terms of dev speed.

## Requirements

- JDK version 8 or above
- sbt
- docker, we use this to run postgresql on ports 25432, 25431 (for testing)

## Running

### the server

If you just want to start the server just run the following from the root of the project:
`./start-server`

This binds the server to port `11312`. N.B. did not setup any port magic forwarding to have it run directly on
localhost.

### tests

Tests require that you have run:
`./ops/docker-postgresql.sh`

We could have tests report this in case they failed to connect. But ow well.

Then it's just `sbt test`

## Things that still need improving

- adding an `spj-json` module that exports all useful things from circe, for the one single `import spj.json.*`
  experience.
- encode `SpjApi` and `ApiResponse` differently, and move exception handling logic to http4s ember exception handler.
- add logging and telemetry
- integration test with http client. Use tapir/smithy4s to define the API, that way we can get free clients for our app.
- use `sbt-native-packager` to package the app instead of relying on `sbt run` to start the server.