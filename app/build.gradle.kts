plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.fourgeailabs.bpwatch"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.fourgeailabs.bpwatch"
    minSdk = 26
    targetSdk = 34
    versionCode = 54
    versionName = "2.07.12"

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
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
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
  from(if (wearDebugApk.exists()) wearDebugApk else fallbackApk)
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

