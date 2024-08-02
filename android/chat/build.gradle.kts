plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

android {
    namespace = "com.joinself.sdk.sample.chat"
    compileSdk = Config.Android.compileSdkVersion

    defaultConfig {
        applicationId = "com.joinself.sdk.sample.chat"
        minSdk = Config.Android.minSdkVersion
        targetSdk = Config.Android.targetSdkVersion
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs {
            pickFirsts.addAll(listOf("lib/x86/libc++_shared.so", "lib/x86_64/libc++_shared.so", "lib/armeabi-v7a/libc++_shared.so", "lib/arm64-v8a/libc++_shared.so",
                "lib/x86/libsodium.so", "lib/x86_64/libsodium.so", "lib/arm64-v8a/libsodium.so",
                "lib/x86/libself_omemo.so", "lib/x86_64/libself_omemo.so", "lib/arm64-v8a/libself_omemo.so"))
            useLegacyPackaging = true
        }
        resources {
            excludes.addAll(listOf("META-INF/DEPENDENCIES.txt", "META-INF/LICENSE.txt", "META-INF/NOTICE.txt",
                "META-INF/NOTICE", "META-INF/LICENSE", "META-INF/DEPENDENCIES",
                "META-INF/notice.txt", "META-INF/license.txt", "META-INF/dependencies.txt",
                "META-INF/LGPL2.1", "META-INF/*.kotlin_module", "META-INF/versions/9/previous-compilation-data.bin", "META-INF/versions/9/OSGI-INF/MANIFEST.MF"))
            excludes.addAll(listOf("DebugProbesKt.bin"))
        }
        dex {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        dataBinding = false
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation("com.joinself:mobile-sdk:1.0.0-SNAPSHOT")
    implementation(project(":common"))

    implementation("com.jakewharton.timber:timber:${Config.Version.timberVersion}")
    implementation("androidx.core:core-ktx:${Config.Version.androidxCore}")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:${Config.Version.materialVersion}")
    implementation("androidx.navigation:navigation-fragment-ktx:${Config.Version.navigationVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${Config.Version.navigationVersion}")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:${Config.Version.navigationVersion}")

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.uiUtil)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(compose.preview)
    implementation("androidx.activity:activity-compose:${Config.Version.activityCompose}")
    implementation("org.jetbrains.androidx.navigation:navigation-compose:${Config.Version.navigationCompose}")
    implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.4.0")

    debugImplementation(compose.uiTooling)

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
}