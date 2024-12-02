import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm") version "1.9.0"
    alias(libs.plugins.indra)
    alias(libs.plugins.indraGit)
    alias(libs.plugins.indraLicenseHeader)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    sonatype.s01Snapshots()
    sonatype.ossSnapshots()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.doesnt-want-to.work/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.noxcrew.com/public")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }
    maven("https://jitpack.io") {
        content { includeGroupByRegex("com\\.github\\..*") }
    }
}

dependencies {
    implementation(platform(kotlin("bom")))

    compileOnly("dev.folia", "folia-api", "1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1")
    compileOnly("me.clip", "placeholderapi", "2.11.6")

    compileOnly("com.sk89q.worldguard", "worldguard-core", "7.0.9")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.9")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.2.9")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.2.9")

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
    implementation("org.spongepowered", "configurate-yaml")
    implementation("org.spongepowered", "configurate-extra-kotlin")

    implementation("org.bstats", "bstats-bukkit", "3.0.2")
    implementation("io.papermc", "paperlib", "1.0.8")

    implementation("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
}

version = (version as String)//.decorateVersion()

java {
    disableAutoTargetJvm()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileKotlin {
        kotlinOptions.javaParameters = true
    }
    jar {
        archiveClassifier.set("not-shadowed")
    }
    shadowJar {
        from(rootProject.file("license.txt")) {
            rename { "license_${rootProject.name.lowercase()}.txt" }
        }

        minimize()
        archiveClassifier.set(null as String?)
        archiveBaseName.set(project.name) // Use uppercase name for final jar

        val prefix = "${project.group}.${project.name.lowercase()}.lib"
        sequenceOf(
            "com.typesafe.config",
            "io.leangen.geantyref",
            "io.papermc.lib",
            "net.kyori",
            "com.brinkmc.pluginbase",
            "cloud.commandframework",
            "org.koin",
            "org.spongepowered.configurate",
            "org.bstats",
            "kotlin",
            "dev.triumphteam",
            "xyz.jpenilla.reflectionremapper",
            "net.fabricmc.mappingio"
        ).forEach { pkg ->
            relocate(pkg, "$prefix.$pkg")
        }

        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    runServer {
        minecraftVersion("1.20.1")
    }
    withType<RunServer> {
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        )
    }
    register("format") {
        group = "formatting"
        description = "Formats source code according to project style."
        dependsOn(licenseFormat, ktlintFormat)
    }
    processResources {
        val props = mapOf(
            "version" to project.version,
            "website" to "https://github.com/VQ9/Plop",
            "description" to project.description,
            "apiVersion" to "1.20",
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

runPaper.folia.registerTask()

//fun String.decorateVersion(): String =
//  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

//fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
//  ?: error("Failed to determine git hash.")
