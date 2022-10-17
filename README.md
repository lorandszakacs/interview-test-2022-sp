# spj - json validation tech test

Tech interview take home test.

Elided adding link or explicit reference to assignment so that people can't just search for the answer on github. I
guess.

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

