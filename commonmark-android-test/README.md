commonmark-android-test
=======================

This module ensures that commonmark-java is supported on Android

Requirements:

* Java 7 or above
* Android SDK 15
* Running emulator or connected android device

Configuration
-----

1. Download Android SDK
2. Be sure that SDK Platform 15 and emulator for this platform (system image) are installed. It's recommended to use x86
3. Export to PATH: `path_to_android_sdk/platform-tools` and `path_to_android_sdk/tools`
4. Create 2 properties files in commonmark-android-test

/local.properties
```properties
sdk.dir=/path_to_android_sdk
```

/test.properties
```properties
# Absolute or relative (./ == /app) path to test reports.
path.report=../report

# Version number of commonmark and extensions in maven central.
version.maven=0.11.0
# Version number of autolink in maven central (not bundled with extension jar).
version.maven_autolink=0.8.0

# Version number of commonmark and extensions in project.
version.snapshot=0.11.1-SNAPSHOT
# Version number of autolink for snapshots (not bundled in extension jar).
version.snapshot_autolink=0.8.0
```

If you're going to test on device with Android 15 then you can skip downloading emulator.

Usage
-----

#### Run test with MAVEN version

on Mac/Linux:
```shell
./gradlew :app:connectedMavenDebugAndroidTest
```

on Windows:
```bat
.\gradlew :app:connectedMavenDebugAndroidTest
```

#### Run test with SNAPSHOT version

Before running tests you need to run `mvn clean install` in the root of
this repository.

on Mac/Linux:
```shell
./gradlew :app:connectedSnapshotDebugAndroidTest
```

on Windows:
```bat
.\gradlew :app:connectedSnapshotDebugAndroidTest
```


#### Testing in CI

on Mac/Linux:
```shell
echo no | android create avd --force -n test -t "android-15"
emulator -avd test &
adb wait-for-device
./gradlew :app:clean :app:connectedSnapshotDebugAndroidTest
adb emu kill
```

on Windows:
```bat
echo no | android create avd --force -n test -t "android-15"
start emulator -avd test
adb wait-for-device
gradlew :app:clean :app:connectedSnapshotDebugAndroidTest & adb emu kill
```

There could be problems with command `adb wait-for-device` which could be resolved by adding additional pause before running test.

Links
-----
[Gradle Documentations](https://docs.gradle.org/current/userguide/userguide.html)
[Android Gradle Plugin Docs](http://tools.android.com/tech-docs/new-build-system)
