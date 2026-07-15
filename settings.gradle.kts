pluginManagement {
    repositories {
        google()
        maven { url = uri("https://repo1.maven.org/maven2") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        maven { url = uri("https://repo1.maven.org/maven2") }
    }
}

rootProject.name = "AppGasto"
include(":app")
include(":benchmark")
