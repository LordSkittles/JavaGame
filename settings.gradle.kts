pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "JavaGame"

// Enable Gradle's configuration cache for better performance
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")