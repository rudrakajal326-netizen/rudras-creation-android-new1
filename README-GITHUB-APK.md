# Rudras Creation — Free GitHub APK Build

This project includes a GitHub Actions workflow that builds an Android APK in the cloud, so you do not need Android Studio on your phone.

## Phone-only steps

1. Create a GitHub account if you do not already have one.
2. Create a new **public** repository (for example `rudras-creation-android`).
3. Upload the **contents of this `rudras_android` folder** to the repository root. The `.github/workflows/build-apk.yml` file must be at exactly that path.
4. Open the repository → **Actions** → **Build Rudras Creation APK**.
5. Tap **Run workflow** → **Run workflow**.
6. Wait for the workflow to finish.
7. Open the completed workflow run. Under **Artifacts**, download `rudras-creation-debug-apk`.
8. Extract the downloaded artifact ZIP and install `app-debug.apk` on the Android phone.

## Notes

- This workflow builds a **debug APK** for testing.
- It does not create a signed Play Store release. A production release needs a signing key and a protected GitHub secret.
- Do not put a Supabase service-role key in the repository or app. Use only the publishable/anonymous client key in the Android app.
- The workflow uses GitHub-hosted Ubuntu, JDK 17, Android SDK, and Gradle 8.9.
