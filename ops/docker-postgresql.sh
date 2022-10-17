#!/usr/bin/env bash
# Script that sets-up our local docker environment

# Running the script for the first time pulls a postgres image from docker repository.
# If a container with the same name already exists, running this script will start/restart the container.
# Postgres configuration is specified by the parameters below.
# Docker is required in order for this script to work !
#
# intended usage for running tests:
# ./ops/docker-postgresql.sh
#
# intended usage for starting the server locally:
# ./ops/docker-postgresql.sh 'local'

# see https://hub.docker.com/_/postgres/tags
# at the time of writing flyway does not support postgresql v 15
POSTGRES_VERSION=14.5-alpine3.16
EXPOSED_PORT=25432              # this is the port on the host machine; most likely you want to change this one.
EXPOSED_PORT_TEST_PORT=25431    # this value is used when the environment is running in "test"
INTERNAL_PORT=5432              # this is the default port on which postgresql starts on within the container.
MAX_CONNECTIONS=115             # default is 115, and since this is only for testing it's OK to just up the number, since we care more about isolation than about performance.
DB_NAME=json_schemas            # use as default config value
DB_PASS="spj_local_password"    # use as default config value
DB_USER="spj_server"            # use as default config value

# added in gitignore. To cleanup everything:
LOCAL_DATA_ROOT=$(realpath ./.runtime/postgres-data)

# attempt to create the DB. does not create directory if it already exists.
[ -d "$LOCAL_DATA_ROOT" ] || mkdir -p "$LOCAL_DATA_ROOT"

# no parameters -> we create database for locally testing (spj_test)
# one parameter (doesn't matter which one), we create database used for running the app locally
if [ -z "$1" ]; then
  EXPOSED_PORT=$EXPOSED_PORT_TEST_PORT # we overwrite it so it doesn't conflict with the "local" version.
  CONTAINER_NAME=postgresql_json_schemas_test # Name of the docker container
  LOCAL_DATA_FOLDER="$LOCAL_DATA_ROOT/latest-test-db"
else
  CONTAINER_NAME=postgresql_json_schemas # Name of the docker container
  LOCAL_DATA_FOLDER="$LOCAL_DATA_ROOT/latest-local-db"
fi

echo "container name: $CONTAINER_NAME"

# this kinda ensures some kind of idempotency
FOUND_CONTAINER=$(docker ps -aq -f name="^/$CONTAINER_NAME$")

if [ "$FOUND_CONTAINER" ]; then
  IS_EXITED=$(docker ps -aq -f name="^/$CONTAINER_NAME$" -f status=exited)
  if [ ! "$IS_EXITED" ]; then
    echo "Stopping postgres container: $FOUND_CONTAINER"
    docker stop $CONTAINER_NAME
  fi
  echo "Starting postgres container: $FOUND_CONTAINER"
  docker start $CONTAINER_NAME
else
  echo "Creating & starting postgres container: '$CONTAINER_NAME'"
  # echo "Data is stored @ $LOCAL_DATA_FOLDER"
  docker run -d \
    --name $CONTAINER_NAME \
    -p $EXPOSED_PORT:$INTERNAL_PORT \
    -e POSTGRES_DB=$DB_NAME \
    -e POSTGRES_USER=$DB_USER \
    -e POSTGRES_PASSWORD=$DB_PASS \
    postgres:$POSTGRES_VERSION -N $MAX_CONNECTIONS
    # hmmm, there seems to be some problems with using this, so for now we use the default postgresql storage location :(
    # postgresql cannot seem to write anything in those folders... so it's probably a permission thing.
    # debugging dev ops problems is fun...
    # -v ${LOCAL_DATA_FOLDER}:/var/lib/postgresql/data \

fi
