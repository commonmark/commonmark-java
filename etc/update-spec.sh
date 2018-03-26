#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <version>"
  exit 1
fi

version=$1
curl -L "https://raw.githubusercontent.com/commonmark/CommonMark/$version/spec.txt" -o commonmark-test-util/src/main/resources/spec.txt

echo "Check cmark and commonmark.js regression.txt:"
echo "https://github.com/commonmark/cmark/blob/master/test/regression.txt"
echo "https://github.com/commonmark/commonmark.js/blob/master/test/regression.txt"
