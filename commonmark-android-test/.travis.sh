#!/bin/sh

set -e
set -x

version=$(cd .. && mvn help:evaluate -Dexpression=project.version | grep -v '^\[' | tail -1)
autolink_version=$(cd ../commonmark-ext-autolink && mvn help:evaluate -Dexpression=autolink.version | grep -v '^\[' | tail -1)

touch test.properties
echo "path.report=../report" >> test.properties
echo "version.maven=0.9.0" >> test.properties
echo "version.maven_autolink=0.10.0" >> test.properties
echo "version.snapshot=$version" >> test.properties
echo "version.snapshot_autolink=$autolink_version" >> test.properties

echo no | android create avd --force -n test -t "android-16"
emulator -avd test -no-audio -no-window &
android-wait-for-emulator

TERM=dumb ./gradlew --stacktrace :app:connectedSnapshotDebugAndroidTest
