# OXCE for Android

OXCE for Android is, as the name suggests, a port of [OXCE](https://openxcom.org/forum/index.php/topic,5251.0.html) to the
Android platform. In order to achieve this, it uses SDL2 set of libraries, and a portion of Java
code for some convenient features like auto-updating files after installing new version.

Uses [afiledialog](https://github.com/jfmdev/afiledialog) library.

# Building

In order to build OXCE for Android, you'll need:

 - Android SDK 28
 - Android NDK 18b

Note: NDK 19b doesn't work! The project compiles, but the game crashes.

Additionally, you'll need Java development kit and Java runtime environment version 1.8 or higher
(Oracle Java SE 8 or OpenJDK 8), which is required for building Android applications.

## Getting the Code

This project uses git submodules, so in order to get the code, you'll have to do the following:

1. Clone this project:


    $ git clone https://github.com/MeridianOXC/openxcom-android.git

    $ cd openxcom-android

    $ git checkout oxce-plus-proto

2. Get submodules


    $ git submodule init

    $ git sibmodule update

3. Since this project uses Android NDK (currently tested with r14b and r18b), you'll need to provide path
to it. Additionally, you'll have to provide path to Android SDK as well. These paths should be in
the local.properties file in the project root. The file should contain the following lines:


    sdk.dir=/path/to/Android/sdk

    ndk.dir=/path/to/Android/ndk

with your own actual paths substituted instead of these placeholders.

# Building the app

At this point you may just run the Gradle wrapper with the `assemble` or `assembleRelease` task:


    $ ./gradlew assembleRelease

The resulting .apk will be in `app/build/outputs/apk` folder.

You might also include some additional data in the apk itself. Everything you put in `app/src/main
/jni/OpenXcom/bin` subdirectories will be packed as assets, so you might include some mods that will be
automatically installed or even the game data that won't require to go through the data copying
process. Note that redistribution of such builds might be illegal in some countries, and as
such they should be only used for debugging purposes.

# Credits

This is a fork of sfalexrog's repository of OpenXcom for Android and most of the actual work has been done by him.

I (Meridian) have only copied and customized his work on this build system.

Many thanks also to Stoddard for helping with all aspects of the project.
