plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/** Short commit SHA baked into the build so the app can detect updates. */
fun gitSha(root: java.io.File): String {
    System.getenv("GITHUB_SHA")?.takeIf { it.length >= 7 }?.let { return it.substring(0, 7) }
    return try {
        ProcessBuilder("git", "rev-parse", "--short=7", "HEAD")
            .directory(root)
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText().trim().ifEmpty { "dev" }
    } catch (e: Exception) {
        "dev"
    }
}

val odakRepo = "NmnRn/to-do-mobile-AI"

android {
    namespace = "com.odak.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.odak.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "GIT_SHA", "\"${gitSha(rootDir)}\"")
        buildConfigField("String", "REPO", "\"$odakRepo\"")
        buildConfigField(
            "String",
            "APK_URL",
            "\"https://github.com/$odakRepo/releases/latest/download/Odak.apk\""
        )
    }

    signingConfigs {
        create("release") {
            val ks = rootProject.file("keystore/odak.p12")
            if (ks.exists()) {
                storeFile = ks
                storePassword = "odakodak"
                keyAlias = "odak"
                keyPassword = "odakodak"
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Stable key (keystore/odak.p12) so in-app updates can install over
            // an existing install; falls back to the debug key if absent.
            signingConfig = if (rootProject.file("keystore/odak.p12").exists())
                signingConfigs.getByName("release")
            else
                signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.ui.tooling)
}
