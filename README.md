# Roll-a-Ball

This project covers the process of integrating a game made with Unity within native Android app.

# Build Prerequisites
* Unity 2019.2.8f1
* Android Studio 3.5.0 or newer
* Latest Android SDK (API 29)
* Android NDK r19 or newer

# Building the Code

**Unity**

You can build the project for any supported platform.
To rebuild the embedding Unity module for Android project:
* Open Build Settings and switch to Android platform.
* Check "Export Project", hit "Export".

**Android**

Android project contains precompiled Unity module as native binaries.

Android app serves as a host for the Unity player with platform UI built on top of it as an overlay.

To build complete application open the project in Android Studio and hit "Run".
