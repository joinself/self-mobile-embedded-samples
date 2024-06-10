plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    composeOptions {
        kotlinCompilerExtensionVersion = Config.Version.composeCompilerVersion
    }
}

dependencies {
    implementation("com.joinself:mobile-sdk:1.0.0-SNAPSHOT")
    implementation(project(":common"))

    implementation("com.jakewharton.timber:timber:${Config.Version.timberVersion}")
    implementation("androidx.core:core-ktx:${Config.Version.androidxCore}")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:${Config.Version.materialVersion}")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:${Config.Version.navigationVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${Config.Version.navigationVersion}")
    implementation("androidx.navigation:navigation-compose:${Config.Version.navigationVersion}")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:${Config.Version.navigationVersion}")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    modules {
        module("com.google.guava:listenablefuture") {
            replacedBy("com.google.guava:guava", "listenablefuture is part of guava")
        }
    }
}