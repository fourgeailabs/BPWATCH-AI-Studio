plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Play release signing: credentials live in keystore.properties at the repo
// root (git-ignored, never committed). The file is absent in CI, where only
// the debug build runs — then the release signing config stays unset and
// project configuration must not fail.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val hasReleaseSigning = keystorePropertiesFile.exists()
val keystoreProperties = mutableMapOf<String, String>()
if (hasReleaseSigning) {
    // Minimal key=value parse. (java.util.Properties doesn't resolve in
    // this build's Kotlin DSL scope, so don't rely on it.)
    keystorePropertiesFile.forEachLine { raw ->
        val line = raw.trim()
        if (line.isNotEmpty() && !line.startsWith("#") && !line.startsWith("!")) {
            val sep = line.indexOfFirst { it == '=' || it == ':' }
            if (sep > 0) {
                keystoreProperties[line.substring(0, sep).trim()] = line.substring(sep + 1).trim()
            }
        }
    }
}

android {
    namespace = "com.fourgeailabs.bpwatch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fourgeailabs.bpwatch"
        minSdk = 30 // Wear OS 3+
        targetSdk = 35
        // Kept in sync with :app (see its versioning policy comment).
        versionCode = 50
        versionName = "2.10.0"
    }

    // signingConfigs is declared before buildTypes: the release build type
    // looks its config up by name when it is configured.
    signingConfigs {
        // Pinned debug keystore shared with :mobile — see the comment there.
        // Both APKs must keep a stable signature or updates wipe user data.
        getByName("debug") {
            storeFile = rootProject.file("keystore/bpwatch-debug.keystore")
            storePassword = "android"
            keyAlias = "bpwatch-debug"
            keyPassword = "android"
        }
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystoreProperties.getValue("storeFile"))
                storePassword = keystoreProperties["storePassword"]
                keyAlias = keystoreProperties["keyAlias"]
                keyPassword = keystoreProperties["keyPassword"]
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    lint {
        // Pre-existing upstream lint findings are recorded in the baseline
        // so release builds pass; NEW lint errors still fail the build.
        baseline = file("lint-baseline.xml")
    }

    buildFeatures {
        compose = true
        // Needed so the watch UI can show its own version (update check).
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val wearComposeVersion = "1.3.1"
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.wear.compose:compose-material:$wearComposeVersion")
    implementation("androidx.wear.compose:compose-foundation:$wearComposeVersion")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    // Watch-face complications (BP / HR / stress, v2.3). ShortText data
    // comes transitively via watchface-complications-data; the suspending
    // service base + update requester live in the -ktx artifact, which is
    // NOT pulled transitively — hence the explicit second line.
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")
    // Wear OS Tile: the swipeable "latest BP + time" card (v2.4.2).
    implementation("androidx.wear.tiles:tiles:1.4.1")
    implementation("com.google.guava:listenablefuture:1.0")

    testImplementation(libs.junit)
}
