import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "eu.thomaskuenneth.souffleur"
version = "1.0.6"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("com.google.zxing:javase:3.4.1")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Souffleur"
            packageVersion = "1.0.6"
            description = "A cross platform remote control for presentations"
            copyright = "2019 - 2022 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
            macOS {
                iconFile.set(project.file("artwork/Souffleur.icns"))
            }
            windows {
                iconFile.set(project.file("artwork/Souffleur.ico"))
                menuGroup = "Thomas Kuenneth"
            }
            modules(
                "java.instrument",
                "java.prefs",
                "jdk.httpserver",
                "jdk.unsupported"
            )
        }
    }
}
