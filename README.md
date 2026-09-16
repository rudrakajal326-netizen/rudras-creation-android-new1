# Rudras Creation — Android Studio Ready Project

This folder is prepared to open directly as an Android Studio project.

## Open and build
1. Extract the ZIP.
2. Open **Android Studio**.
3. Choose **Open** and select the extracted `rudras_android` folder (the folder containing `settings.gradle.kts`).
4. Let Android Studio sync/download the Gradle and Android dependencies.
5. Connect an Android phone with USB debugging enabled, or start an emulator.
6. Press **Run ▶** to test the app.
7. For an APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
8. Android Studio will show the APK location when the build finishes.

## Project settings
- Application ID: `com.rudras.creation`
- Minimum Android: API 26
- Target Android: API 35
- Compile SDK: 35
- Kotlin: 2.0.21
- Android Gradle Plugin: 8.7.3

## Backend
The app is configured to use the supplied Rudras Creation Supabase project through the public/publishable client key. Never put a Supabase service-role key in the Android app.

Before production, apply the SQL migration in `supabase/migrations/20260916_app_commerce.sql` and verify the project's RLS/security rules.

## Important
This environment does not have the Android SDK/Gradle distribution installed, so an installable APK could not be compiled here. The ZIP is therefore an Android Studio project, not an APK.

## Free phone-only APK build

A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`. See `README-GITHUB-APK.md` for the phone-only steps. It builds a debug APK and uploads it as a downloadable GitHub Actions artifact.
