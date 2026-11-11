plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

// Play release signing: credentials live in keystore.properties at the repo
// root (git-ignored, never committed). The file is absent in CI, where only
// the debug build runs — then the release signing config stays unset and
// project configuration must not fail.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val hasReleaseSigning = keystorePropertiesFile.exists()
val keystoreProperties = mutableMapOf<String, String>()
if (hasReleaseSigning) {
  // Minimal key=value parse. (java.util.Properties doesn't resolve in this
  // build's Kotlin DSL scope, so don't rely on it.)
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
    minSdk = 26
    targetSdk = 35
    versionCode = 68
    versionName = "2.11.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    getByName("debug") {
      val pinnedKeystore = rootProject.file("keystore/bpwatch-debug.keystore")
      storeFile = if (pinnedKeystore.exists()) pinnedKeystore else file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = if (pinnedKeystore.exists()) "bpwatch-debug" else "androiddebugkey"
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
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging {
    resources {
      excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
      excludes += "META-INF/LICENSE.md"
      excludes += "META-INF/NOTICE.md"
    }
  }
}

val bundleWearApk = tasks.register<Copy>("bundleWearApk") {
  dependsOn(":wear:assembleDebug")
  val wearDebugApk = rootProject.file("wear/build/outputs/apk/debug/wear-debug.apk")
  val fallbackApk = rootProject.file("releases/BPWatch-Wear-GalaxyWatch-debug.apk")
  from(provider { if (wearDebugApk.exists()) wearDebugApk else fallbackApk })
  into(file("src/main/assets"))
  rename { "bpwatch-wear.apk" }
}

tasks.named("preBuild") { dependsOn(bundleWearApk) }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation("androidx.compose.material3:material3:1.3.1")
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.runtime.compose)

  implementation("com.google.android.gms:play-services-wearable:18.1.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

  implementation("androidx.health.connect:connect-client:1.1.0-alpha11")

  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  implementation(libs.androidx.datastore.preferences)

  implementation("org.conscrypt:conscrypt-android:2.5.3")
  implementation("org.bouncycastle:bcprov-jdk18on:1.86")
  implementation("org.bouncycastle:bcpkix-jdk18on:1.86")

  testImplementation(libs.junit)
  debugImplementation(libs.androidx.compose.ui.tooling)
}

