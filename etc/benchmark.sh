#!/bin/sh

cd $(dirname $0)/..
mvn -pl commonmark -Pbenchmark -DskipTests clean package exec:exec
