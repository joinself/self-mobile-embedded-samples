
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
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
        classpath("com.squareup.wire:wire-gradle-plugin:4.9.7")
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

}

allprojects {
    configurations.all {
        resolutionStrategy {
            cacheDynamicVersionsFor(5, TimeUnit.MINUTES)
            cacheChangingModulesFor(0, TimeUnit.SECONDS)
        }
    }
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