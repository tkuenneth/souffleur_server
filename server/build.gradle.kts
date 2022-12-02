import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "eu.thomaskuenneth.souffleur"
version = "1.0.7"

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
    val macos = listOf(macosX64(), macosArm64())
    configure(macos) {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("com.google.zxing:javase:3.4.1")
                implementation( "org.jetbrains.compose.material:material-icons-extended-desktop:1.2.0")
            }
        }
        val jvmTest by getting

        val macosX64Main by getting {
            dependsOn(commonMain)
        }
        val macosX64Test by getting

        val macosArm64Main by getting {
            dependsOn(macosX64Main)
        }
        val macosArm64Test by getting
    }
}

compose.desktop {
    application {
        mainClass = "eu.thomaskuenneth.souffleur.ComposeMainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Souffleur"
            packageVersion = "1.0.7"
            description = "A cross platform remote control for presentations"
            copyright = "2019 - 2022 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
            macOS {
                bundleID = "eu.thomaskuenneth.souffleur"
                iconFile.set(project.file("artwork/Souffleur.icns"))
                signing {
                    sign.set(true)
                    identity.set("Thomas Kuenneth")
                }
                notarization {
                    appleID.set("thomas.kuenneth@icloud.com")
                    password.set("@keychain:NOTARIZATION_PASSWORD")
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
