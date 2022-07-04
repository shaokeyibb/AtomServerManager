import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "io.hikarilan.atomservermanager"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}

tasks.withType(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}

compose.desktop {
    application {
        mainClass = "io.hikarilan.atomservermanager.MainKt"
//        nativeDistributions {
//            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
//            packageVersion = version as String
//        }
    }
}