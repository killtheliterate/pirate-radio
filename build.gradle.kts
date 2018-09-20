import org.jetbrains.kotlin.gradle.frontend.FrontendPlugin
import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.npm.NpmExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationJsExtension

group = "me.theghostin"
version = "1.0-SNAPSHOT"

plugins {
    id("kotlin-platform-js") version "1.2.61"
}

// The Kotlin Frontend plugin currently only supports the legacy gradle plugin application
buildscript {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://kotlin.bintray.com/kotlinx")

    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-frontend-plugin:0.0.37")
        classpath("org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin:0.6.1")
    }
}
apply<FrontendPlugin>()

repositories {
    jcenter()
    maven("https://dl.bintray.com/danfma/kotlin-kodando")
    maven("https://dl.bintray.com/spookyspecter/me.theghostin")
}

dependencies {
    compile(kotlin("stdlib-js"))
    compile("org.jetbrains.kotlinx:kotlinx-html-js:0.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:0.24.0")
    compile("br.danfma.kodando:kodando-history:0.5.0")
    compile("me.theghostin:me.theghostin.nimble:1.0.1")
}

configure<KotlinFrontendExtension> {
    bundle<WebPackExtension>("webpack") { when (this) {
        is WebPackExtension -> {
            bundleName = project.name
        }
    } }
}
configure<NpmExtension> {
    dependency("kotlinx-coroutines-core")
    dependency("history")
    dependency("nanomorph")

    devDependency("html-webpack-plugin")
    devDependency("html-webpack-include-assets-plugin")
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            outputFile =  "${buildDir.path}/js/${project.name}.js"
            sourceMap = true
            metaInfo = true
            moduleKind = "commonjs"
            main = "call"
        }
    }
    "deployToETR"(Copy::class) {
        description = "copy the build bundle to /pirate-radio on eattherich.club"
        from("${buildDir.path}/bundle/")
        into("K:/team/eat_the_rich/pirate-radio")
        dependsOn("bundle")
    }
}
