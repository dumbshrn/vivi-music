import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.music.vivi"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        optIn.add("kotlin.io.encoding.ExperimentalEncodingApi")
    }
}

javafx {
    version = "21.0.7"
    modules = listOf("javafx.media")
}

sourceSets {
    main {
        kotlin.srcDir("../innertube/src/main/kotlin")
        kotlin.srcDir("../lyricsProvider/src/main/kotlin")
        kotlin.exclude("com/music/paxsenix/**")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation("com.materialkolor:material-kolor:3.0.0-beta01")
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    implementation("io.ktor:ktor-client-core:3.2.0")
    implementation("io.ktor:ktor-client-okhttp:3.2.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.0")
    implementation("io.ktor:ktor-client-encoding:3.2.0")
    implementation("io.ktor:ktor-client-cio:3.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.brotli:dec:0.1.2")

    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.26.1")
}

tasks.register<JavaExec>("smoke") {
    group = "verification"
    description = "Headless check of the ported Innertube + lyrics layer"
    mainClass.set("com.music.vivi.desktop.SmokeKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.register<JavaExec>("screenshots") {
    group = "verification"
    description = "Renders the Compose UI off-screen to build/screenshots"
    mainClass.set("com.music.vivi.desktop.PreviewKt")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("-Dskiko.renderApi=SOFTWARE", "-Djava.awt.headless=true")
    workingDir = project.projectDir
    standardOutput = System.out
}

compose.desktop {
    application {
        mainClass = "com.music.vivi.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "VIVIMusic"
            packageVersion = "1.0.0"
            description = "VIVI Music — Material You YouTube Music client for Windows"
            vendor = "VIVI Music"
            windows {
                iconFile.set(project.file("icons/vivi.ico"))
                menuGroup = "VIVI Music"
                shortcut = true
                dirChooser = true
                perUserInstall = true
            }
        }
    }
}
