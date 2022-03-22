#!/bin/bash


# Necessary value(s)
_here="$(pwd)"
_whereAndroidSDK="/external/0/android-app-development"
_whereKeystore="${HOME}/keystore.jks"
_nameKey="key1"
_versionAndroid="10.0.0"
_versionSDK="29"
_versionToolchain="30.0.3"


# Function(s)
function clean() {
	rm -rf classes.dex outlet proguard_options public_resources.xml sources-*.txt unfinished.apk finished.apk || return 1
	for x in $(find . -name R.java); do
		rm -f $x || return 1
	done
}


# Initialize
echo ":: Initialize"
clean || exit 1
if (test "$1" = "justclean"); then
	exit 0
fi
mkdir -p assets outlet || exit 1

# R.java
echo ":: R.java"
${_whereAndroidSDK}/build-tools/${_versionToolchain}/aapt package -m --auto-add-overlay \
	-J sources \
	-A assets \
	-M AndroidManifest.xml \
	-P public_resources.xml \
	-G proguard_options \
	--min-sdk-version ${_versionSDK} \
	--target-sdk-version ${_versionSDK} \
	--version-code ${_versionSDK} \
	--version-name ${_versionAndroid} \
	-S resources \
	-I ${_whereAndroidSDK}/platforms/android-${_versionSDK}/android.jar \
|| exit 1

# JVM bytecode
echo ":: JVM bytecode"
find sources -name '*.java' >> sources-unsorted.txt
tr ' ' '\n' < sources-unsorted.txt | sort -u > sources-sorted.txt
javac -d outlet -classpath ${_whereAndroidSDK}/platforms/android-${_versionSDK}/android.jar -sourcepath sources @sources-sorted.txt || exit 1

# Dalvik bytecode
echo ":: Dalvik bytecode"
${_whereAndroidSDK}/build-tools/${_versionToolchain}/d8 --release \
	$(find outlet -name "*.class") \
|| exit 1

# APK
echo ":: APK"
${_whereAndroidSDK}/build-tools/${_versionToolchain}/aapt package -u --auto-add-overlay \
	-M AndroidManifest.xml \
	-A assets \
	--min-sdk-version ${_versionSDK} \
	--target-sdk-version ${_versionSDK} \
	--version-code ${_versionSDK} \
	--version-name ${_versionAndroid} \
	-S resources \
	-I ${_whereAndroidSDK}/platforms/android-${_versionSDK}/android.jar \
	-F unfinished.apk \
|| exit 1

# Include Dalvik bytecode into APK
echo ":: Include Dalvik bytecode into APK"
zip -qj unfinished.apk classes.dex || exit 1

# Sign APK
echo ":: Sign APK"
jarsigner -keystore ${_whereKeystore} unfinished.apk ${_nameKey} || exit 1
	
# Realign data in APK
echo ":: Realign data in APK"
${_whereAndroidSDK}/build-tools/${_versionToolchain}/zipalign -f 4 unfinished.apk finished.apk || exit 1

# Done
echo ":: Done"
