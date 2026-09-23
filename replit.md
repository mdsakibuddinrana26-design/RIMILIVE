# Building the Android app on Replit

Java 21 is provided by `.replit`. Run `bash scripts/build-android.sh` from the project root. On the first build, this installs the Android SDK command-line tools, API 36.1 platform, and build tools into the ignored `.android-sdk/` directory. It also generates the ignored `debug.keystore` required by the existing debug signing configuration. Gradle downloads its own version through the project's wrapper.

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. This is a native Android project, so it has no web preview or persistent server workflow; install the APK on an Android device or emulator to run it. The debug keystore generated here is local to this workspace: Google/Firebase sign-in may need its SHA fingerprint registered if it relies on debug signing.

No Gemini API key is configured for this build. To use features that require one, configure it separately as described by `.env.example`; do not commit secrets to the repository.