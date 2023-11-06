
buildscript {
    repositories {
        google()
        mavenCentral()

        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://artifactory.joinself.com/artifactory/libs-release")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Config.Version.androidGradlePluginVersion}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Config.Version.kotlinVersion}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${Config.Version.kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.4")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("com.github.triplet.gradle:play-publisher:3.8.1")
        classpath("com.gladed.androidgitversion:gradle-android-git-version:0.4.14")
        classpath("gradle.plugin.com.hierynomus.gradle.plugins:license-gradle-plugin:0.16.1")
        classpath("com.squareup.wire:wire-gradle-plugin:4.9.1")
    }

    configurations.all {
        resolutionStrategy {
            cacheDynamicVersionsFor(5, TimeUnit.MINUTES)
            cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
    }
}

plugins {
    id("com.android.application") version Config.Version.androidGradlePluginVersion apply false
    id("com.android.library") version Config.Version.androidGradlePluginVersion apply false
    id("org.jetbrains.kotlin.android") version Config.Version.kotlinVersion apply false
    id("org.jetbrains.kotlin.kapt") version Config.Version.kotlinVersion apply false
    kotlin("multiplatform").version(Config.Version.kotlinVersion).apply(false)
    kotlin("native.cocoapods").version(Config.Version.kotlinVersion).apply(false)
    id("io.realm.kotlin") version Config.Version.realmVersion apply false
    id("com.google.dagger.hilt.android") version Config.Version.hiltVersion apply false
}

allprojects {
}
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
}
tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}