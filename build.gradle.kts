import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.*
import java.io.*

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "eu.thomaskuenneth.souffleur"
val properties = Properties()
val file = rootProject.file("src/main/resources/version.properties")
if (file.isFile) {
    InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { reader ->
        properties.load(reader)
    }
} else error("${file.absolutePath} not found")
version = properties.getProperty("VERSION")

val appleId = System.getenv("PROD_MACOS_NOTARIZATION_APPLE_ID") ?: ""
val appleTeamId = System.getenv("PROD_MACOS_NOTARIZATION_TEAM_ID") ?: ""
val notarizationPassword = System.getenv("PROD_MACOS_NOTARIZATION_PWD") ?: ""

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation("com.google.zxing:javase:3.4.1")
    implementation( "org.jetbrains.compose.material:material-icons-extended-desktop:1.2.0")
}

compose.desktop {
    application {
        mainClass = "eu.thomaskuenneth.souffleur.ComposeMainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Souffleur"
            packageVersion = version.toString()
            description = "A cross platform remote control for presentations"
            copyright = "2019 - 2024 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
            macOS {
                bundleID = "eu.thomaskuenneth.souffleur"
                iconFile.set(project.file("artwork/Souffleur.icns"))
                signing {
                    sign.set(true)
                    identity.set("Thomas Kuenneth")
                }
                notarization {
                    appleID.set(appleId)
                    password.set(notarizationPassword)
                    teamID.set(appleTeamId)
                }
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
        buildTypes.release.proguard {
            configurationFiles.from("rules.pro")
        }
    }
}
