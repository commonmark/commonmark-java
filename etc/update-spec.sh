#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <version>"
  exit 1
fi

version=$1
curl -L "https://raw.githubusercontent.com/jgm/CommonMark/$version/spec.txt" -o commonmark-test-util/src/main/resources/spec.txt
