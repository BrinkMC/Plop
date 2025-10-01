import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm") version "2.2.0"
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
    maven("https://jitpack.io")
    maven("https://repo.doesnt-want-to.work/snapshots/")
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))

    compileOnly("io.papermc.paper", "paper-api", "1.21.7-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1")
    compileOnly("me.clip", "placeholderapi", "2.11.6")
    compileOnly("me.glaremasters", "guilds", "3.5.7.0")
    compileOnly("io.lumine", "Mythic-Dist", "5.10.0-SNAPSHOT")
    compileOnly("com.github.yannicklamprecht:worldborderapi:1.217.0:dev") // Weird import
    compileOnly("com.github.retrooper", "packetevents-spigot", "2.7.0")
    compileOnly("de.oliver", "FancyHolograms", "2.7.0")


    compileOnly("com.sk89q.worldguard", "worldguard-core", "7.0.15-SNAPSHOT")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.15-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.3.15-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.3.15-SNAPSHOT")

    // Kyori
    compileOnly(platform("net.kyori:adventure-bom:4.17.0"))
    compileOnly("net.kyori", "adventure-extra-kotlin")
    compileOnly("net.kyori", "adventure-serializer-configurate4")

    implementation(platform("org.incendo:cloud-bom:2.0.0"))
    implementation("org.incendo","cloud-annotations")
    implementation("org.incendo","cloud-kotlin-coroutines-annotations")
    implementation("org.incendo","cloud-kotlin-extensions")
    implementation("org.incendo","cloud-kotlin-coroutines")
    implementation("org.incendo","cloud-paper", "2.0.0-beta.10")


    // Coroutine implementation
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.github.shynixn.mccoroutine", "mccoroutine-bukkit-api","2.22.0")
    implementation("com.github.shynixn.mccoroutine", "mccoroutine-bukkit-core","2.22.0")
    implementation("com.noxcrew.interfaces", "interfaces", "2.0.0") {
        exclude(group = "com.google.guava")
    }

    implementation(platform("org.spongepowered:configurate-bom:4.1.2"))
    implementation("org.spongepowered", "configurate-hocon")
    implementation("org.spongepowered", "configurate-extra-kotlin")

    implementation("com.zaxxer", "HikariCP", "6.2.1")
    implementation("com.sksamuel.aedile", "aedile-core", "3.0.1")
    implementation("org.bstats", "bstats-bukkit", "3.0.2")
}

version = (version as String)//.decorateVersion()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            javaParameters.set(true)
        }
    }
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
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
//    runServer {
//        minecraftVersion("1.20.1")
//    }
    register("format") {
        group = "formatting"
        description = "Formats source code according to project style."
        dependsOn( ktlintFormat)
    }
}

//runPaper.folia.registerTask()

//fun String.decorateVersion(): String =
//  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

//fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
//  ?: error("Failed to determine git hash.")
