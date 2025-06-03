enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://jitpack.io") // Aggiunto qui
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // Aggiunto anche qui
    }
}

rootProject.name = "My_Application"
include(":CrewHiveAndroid")
include(":shared")
