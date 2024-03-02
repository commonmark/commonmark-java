commonmark-android-test
=======================

This module ensures that commonmark-java is supported on Android by running `lint` checks on library sources.
Current `minSdk` is 19

Requirements:

* Java 11 or above
* Android SDK 30

Configuration
-----

1. Download Android SDK
2. Be sure that SDK Platform 30 is installed. It's recommended to use x86
3. Export to PATH: `path_to_android_sdk/platform-tools` and `path_to_android_sdk/tools`
4. Create 2 properties files in commonmark-android-test

/local.properties
```properties
sdk.dir=/path_to_android_sdk
```

Usage
-----

#### Run lint checked

on Mac/Linux:
```shell
./gradlew :app:lint
```

on Windows:
```bat
.\gradlew :app:lint
```

Links
-----
[Gradle Documentations](https://docs.gradle.org/current/userguide/userguide.html)
[Android Gradle Plugin Docs](http://tools.android.com/tech-docs/new-build-system)
