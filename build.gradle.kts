import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "2.3.0"
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    google()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://maven.noxcrew.com/public")
    maven("https://jitpack.io/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.fancyplugins.de/releases")
    maven("https://eldonexus.de/repository/maven-releases/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))

    // Hooks
    compileOnly("dev.folia:folia-api:26.1.2.build.+")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("me.glaremasters:guilds:3.5.7.0")
    compileOnly("io.lumine:Mythic-Dist:5.12.0-SNAPSHOT")
    compileOnly("com.github.yannicklamprecht:worldborderapi:1.2111.0:dev") // Weird import
    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
    compileOnly("de.oliver:FancyHolograms:2.9.1")

    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.17-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17-SNAPSHOT"){
        isTransitive = false
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.4.4-SNAPSHOT"){
        isTransitive = false
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.4-SNAPSHOT"){
        isTransitive = false
    }

    // Kyori
    compileOnly(platform("net.kyori:adventure-bom:4.26.1"))
    compileOnly("net.kyori", "adventure-extra-kotlin")
    compileOnly("net.kyori", "adventure-serializer-configurate4")

    // Commands
    implementation(platform("org.incendo:cloud-bom:2.0.0"))
    implementation("org.incendo","cloud-annotations")
    implementation("org.incendo","cloud-kotlin-coroutines-annotations")
    implementation("org.incendo","cloud-kotlin-extensions")
    implementation("org.incendo","cloud-kotlin-coroutines")
    implementation("org.incendo","cloud-paper", "2.0.0-beta.10")


    // Coroutine implementation
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.22.0")
    implementation("com.noxcrew.interfaces:interfaces:2.1.0-SNAPSHOT") {
        exclude(group = "com.google.guava")
    }

    // Configs
    implementation(platform("org.spongepowered:configurate-bom:4.2.0"))
    implementation("org.spongepowered", "configurate-hocon")
    implementation("org.spongepowered", "configurate-extra-kotlin")

    // Database
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.sksamuel.aedile:aedile-core:3.0.2")
}

version = (version as String)//.decorateVersion()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
            javaParameters.set(true)
        }
    }
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
    jar {
        archiveClassifier.set("not-shadowed")
    }
    shadowJar {
        from(rootProject.file("license.txt")) {
            rename { "license_${rootProject.name.lowercase()}.txt" }
        }

        archiveClassifier.set(null as String?)
        archiveBaseName.set(project.name) // Use uppercase name for final jar

        val prefix = "com.brinkmc.plop.lib"
        sequenceOf(
            "com.typesafe.config",
            "io.leangen.geantyref",
            "io.papermc.lib",
            "com.brinkmc.pluginbase",
            "org.incendo",
            "com.google.common",
            "com.noxcrew",
            "org.koin",
            "com.zaxxer",
            "org.spongepowered.configurate",
            "com.github.benmanes",
            "org.bstats",
            "net.fabricmc.mappingio",
            "com.github.ben-manes.caffeine",
            "com.sksamuel.aedile"
        ).forEach { pkg ->
            relocate(pkg, "$prefix.$pkg")
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    register("format") {
        group = "formatting"
        description = "Formats source code according to project style."
        dependsOn( ktlintFormat)
    }
}

