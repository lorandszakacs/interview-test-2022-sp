#!/usr/bin/env bash
set -e
# Starts the local docker container for the server, clean compiles the server, and starts it in sbt.

echo "Please make sure you start the script from the root of your project:"
echo "    ./start-server.sh"
echo ""
echo "starting docker w/ postgresql if it's not already started"
echo ""
./ops/docker-postgresql.sh local

echo "telling sbt to build and run our server. We did not setup packaging using sbt-native-packager"
echo ""
sbt runServer