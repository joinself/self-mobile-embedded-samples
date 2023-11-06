plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.joinself.sdk.sample"
    compileSdk = Config.Android.compileSdkVersion

    defaultConfig {
        applicationId = "com.joinself.sdk.sample"
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
                "META-INF/LGPL2.1", "META-INF/*.kotlin_module", "META-INF/versions/9/previous-compilation-data.bin"))
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
    }
}

dependencies {
    implementation("com.joinself:mobile-sdk:1.0.0-SNAPSHOT")

    implementation("com.jakewharton.timber:timber:${Config.Version.timberVersion}")
    implementation("androidx.core:core-ktx:${Config.Version.androidxCore}")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:${Config.Version.materialVersion}")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:${Config.Version.navigationVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${Config.Version.navigationVersion}")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
}