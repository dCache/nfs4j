#!/bin/sh

set -e
cd "$(dirname $0)"/..
mvn verify -Dreformat -DskipTests
