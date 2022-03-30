# Mini F
Tiny F-Droid client\
Version 2.0

## How to build

1. Prepare JKS keystore
	1. If you don't have, create a new one ([Guide](https://tutorialspedia.com/java-keytool-step-by-step-tutorial-generate-jks-keystore-using-keytool-and-export-certificate-from-keystore/))
2. Open ./build.sh with text editor
3. Modify the value for
	1. _whereAndroidSDK (Path to Android SDK)
	2. _whereKeystore (Path to JKS Keystore file)
	3. _nameKey (Name of key in keystore file)
	4. _versionAndroid (Version of Android)
	5. _versionSDK (Version of SDK)
	6. _versionToolchain (Version of build-tools)
4. Run ./build.sh (APK will be stored as ./finished.apk)

## How to use

1. Install Mini F on your Android device
2. Download index-v1.jar on PC: https://f-droid.org/repo/index-v1.jar
3. Connect your Android device to PC
	1. Maybe you have to enable Developer Options and USB Debugging
4. Run these on PC
	1. `adb shell mkdir -p /sdcard/Android/data/whitestone8214.frontend.minif/files`
	2. `adb push /path/to/index-v1.jar /sdcard/Android/data/whitestone8214.frontend.minif/files/`
5. Launch Mini F
6. Tap "Load" button on top of window and wait (For me it took about 10 minutes)
	1. After this is done, you can just tap "Previous 100" or "Next 100" on relaunch to see the list

## Note

1. Gradle and/or Android Studio is not supported, and have no plan to do
