import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.*
import java.io.*

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "eu.thomaskuenneth.souffleur"
val properties = Properties()
val file = rootProject.file("src/jvmMain/resources/version.properties")
if (file.isFile) {
    InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { reader ->
        properties.load(reader)
    }
} else error("${file.absolutePath} not found")
version = properties.getProperty("VERSION")

val appleId = System.getenv("PROD_MACOS_NOTARIZATION_APPLE_ID") ?: "".also {
    if (it.isEmpty()) error("PROD_MACOS_NOTARIZATION_APPLE_ID is empty")
}
val appleTeamId = System.getenv("PROD_MACOS_NOTARIZATION_TEAM_ID") ?: "".also {
    if (it.isEmpty()) error("PROD_MACOS_NOTARIZATION_TEAM_ID is empty")
}
val notarizationPassword = System.getenv("NOTARIZATION_PASSWORD") ?: "".also {
    if (it.isEmpty()) error("NOTARIZATION_PASSWORD is empty")
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    val macos = listOf(macosX64(), macosArm64())
    configure(macos) {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation("com.google.zxing:javase:3.4.1")
                implementation( "org.jetbrains.compose.material:material-icons-extended-desktop:1.2.0")
            }
        }
        val jvmTest by getting
        val macosX64Main by getting
        val macosX64Test by getting
        val macosArm64Main by getting
        val macosArm64Test by getting
    }
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
