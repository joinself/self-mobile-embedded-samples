plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.joinself.sdk.sample.chat.compose"
    compileSdk = Config.Android.compileSdkVersion

    defaultConfig {
        applicationId = "com.joinself.sdk.sample.chat.compose"
        minSdk = Config.Android.minSdkVersion
        targetSdk = Config.Android.targetSdkVersion
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
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
                "META-INF/LGPL2.1", "META-INF/*.kotlin_module", "META-INF/versions/9/previous-compilation-data.bin"))
        }
        dex {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation("com.joinself:mobile-sdk:1.0.0-SNAPSHOT")
    implementation(project(":common"))
//    implementation(project(":self-android-sdk"))

    implementation("com.jakewharton.timber:timber:${Config.Version.timberVersion}")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.23.0")
    implementation("com.google.accompanist:accompanist-permissions:0.19.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
}