plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val updateManifestUrl = providers.gradleProperty("rudrasUpdateManifestUrl").orNull.orEmpty()
val useTemporaryTestSigning = providers.gradleProperty("useTemporaryTestSigning").orNull == "true"
val releaseStoreFile = providers.gradleProperty("releaseStoreFile").orNull
val releaseStorePassword = providers.gradleProperty("releaseStorePassword").orNull
val releaseKeyAlias = providers.gradleProperty("releaseKeyAlias").orNull
val releaseKeyPassword = providers.gradleProperty("releaseKeyPassword").orNull

android {
    namespace = "com.rudras.creation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rudras.creation"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
        buildConfigField("String", "UPDATE_MANIFEST_URL", "\"${updateManifestUrl}\"")
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("production") {
                storeFile = file(releaseStoreFile)
                storePassword = requireNotNull(releaseStorePassword) { "releaseStorePassword is required with releaseStoreFile" }
                keyAlias = requireNotNull(releaseKeyAlias) { "releaseKeyAlias is required with releaseStoreFile" }
                keyPassword = requireNotNull(releaseKeyPassword) { "releaseKeyPassword is required with releaseStoreFile" }
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = when {
                useTemporaryTestSigning -> signingConfigs.getByName("debug")
                releaseStoreFile != null -> signingConfigs.getByName("production")
                else -> null
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
