rootProject.name = "SelfSDK Samples"
pluginManagement {

    /**
     * The pluginManagement {repositories {...}} block configures the
     * repositories Gradle uses to search or download the Gradle plugins and
     * their transitive dependencies. Gradle pre-configures support for remote
     * repositories such as JCenter, Maven Central, and Ivy. You can also use
     * local repositories or define your own remote repositories. The code below
     * defines the Gradle Plugin Portal, Google's Maven repository,
     * and the Maven Central Repository as the repositories Gradle should use to look for its dependencies.
     */

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {

    /**
     * The dependencyResolutionManagement { repositories {...}}
     * block is where you configure the repositories and dependencies used by
     * all modules in your project, such as libraries that you are using to
     * create your application. However, you should configure module-specific
     * dependencies in each module-level build.gradle file. For new projects,
     * Android Studio includes Google's Maven repository
     * and the Maven Central Repository by
     * default, but it does not configure any dependencies (unless you select a
     * template that requires some).
     */

    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        flatDir {
            dirs("libs")
        }
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://maven.microblink.com") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://zendesk.jfrog.io/zendesk/repo") }
        maven { url = uri("https://maven.scijava.org/content/repositories/public/") }
        maven { url = uri("https://jitpack.io")  }
    }
}

include(":common")
include(":chat")
include(":chat-compose")
//include(":sign-in")
