rootProject.name = "pirateradio"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "kotlin-platform-js",
                "kotlin-platform-common",
                "kotlin-platform-jvm" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.frontend" ->
                    useModule("org.jetbrains.kotlin:kotlin-frontend-plugin:${requested.version}")
            }
        }
    }
}
