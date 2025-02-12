import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm") version "1.9.25"
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.doesnt-want-to.work/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.noxcrew.com/public")
    maven("https://eldonexus.de/repository/maven-releases/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))

    compileOnly("dev.folia", "folia-api", "1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1")
    compileOnly("me.clip", "placeholderapi", "2.11.6")
    compileOnly("me.glaremasters", "guilds", "3.5.7.0")
    compileOnly("io.lumine", "Mythic-Dist", "5.7.2")
    compileOnly(fileTree("libs/") { include("*.jar") })
    compileOnly("com.github.yannicklamprecht:worldborderapi:1.201.0:dev") // Weird import


    compileOnly("com.sk89q.worldguard", "worldguard-core", "7.0.11")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.11")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.3.9")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.3.9")

    // Kyori
    implementation(platform("net.kyori:adventure-bom:4.17.0"))
    implementation("net.kyori", "adventure-extra-kotlin")
    implementation("net.kyori", "adventure-serializer-configurate4")

    implementation(platform("org.incendo:cloud-bom:2.0.0"))
    implementation("org.incendo","cloud-annotations")
    implementation("org.incendo","cloud-kotlin-coroutines-annotations")
    implementation("org.incendo","cloud-kotlin-extensions")
    implementation("org.incendo","cloud-kotlin-coroutines")
    implementation("org.incendo","cloud-paper", "2.0.0-beta.10")


    // Coroutine implementation
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.github.shynixn.mccoroutine", "mccoroutine-bukkit-api","2.20.0")
    implementation("com.github.shynixn.mccoroutine", "mccoroutine-bukkit-core","2.20.0")
    implementation("com.noxcrew.interfaces", "interfaces", "1.2.0")

    implementation(platform("org.spongepowered:configurate-bom:4.1.2"))
    implementation("org.spongepowered", "configurate-hocon")
    implementation("org.spongepowered", "configurate-extra-kotlin")

    implementation("com.zaxxer", "HikariCP", "6.2.1")
    implementation("org.bstats", "bstats-bukkit", "3.0.2")
    implementation("io.papermc", "paperlib", "1.0.8")
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
        kotlinOptions {
            jvmTarget = "21"
            javaParameters = true
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
            "net.kyori",
            "com.brinkmc.pluginbase",
            "org.incendo",
            "com.noxcrew",
            "org.koin",
            "com.zaxxer",
            "org.spongepowered.configurate",
            "com.github.benmanes",
            "org.bstats",
            "net.fabricmc.mappingio"
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
