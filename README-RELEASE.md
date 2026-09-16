# Rudras Creation Android Release

The Android app reads catalogue data and product images from the existing Supabase project. Product and content changes do not require a new APK.

## App updates

The update checker is disabled when no manifest URL is configured. When enabled, it only accepts an HTTPS manifest and an HTTPS APK URL, and only offers an update when the manifest version code is greater than the installed version.

Example manifest:

    {
      "versionCode": 3,
      "versionName": "1.2.0",
      "apkUrl": "https://your-domain.example/releases/rudras-creation-1.2.0.apk",
      "notes": "New collection and seasonal improvements."
    }

Run the GitHub Actions workflow manually and provide the manifest URL in the optional update_manifest_url input. The app update prompt is informational; it does not force-install anything.

## APK build

The workflow builds the release variant with JDK 17, Android API 35, Build Tools 35.0.0, and Gradle 8.13. It uploads rudras-creation-release-apk.

The current CI artifact is unsigned because no production signing key is stored in the repository. Before public distribution, configure a stable release keystore through protected GitHub Actions secrets. Never commit the keystore or its passwords.

