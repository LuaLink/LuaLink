import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.modrinth.minotaur") version "2.8.7"
    id("io.papermc.hangar-publish-plugin") version "0.1.1"
    id("maven-publish")
    application
}

val buildNum = System.getenv("GITHUB_RUN_NUMBER") ?: "SNAPSHOT"

group = "xyz.galaxyy.lualink"

version = "1.20.4-$buildNum"

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val cloudVersion: String by project
val luaKTVersion: String by project

dependencies {
    testImplementation(kotlin("test"))
    library(kotlin("stdlib"))
    api(kotlin("stdlib"))
    compileOnly("org.purpurmc.purpur:purpur-api:1.20.1-R0.1-SNAPSHOT")
    library("com.github.only52607.luakt:luakt:$luaKTVersion")
    library("com.github.only52607.luakt:luakt-core:$luaKTVersion")
    library("com.github.only52607.luakt:luakt-extension:$luaKTVersion")
    library("com.github.only52607.luakt:luakt-luaj:$luaKTVersion")
    api("com.github.only52607.luakt:luakt:$luaKTVersion")
    api("com.github.only52607.luakt:luakt-core:$luaKTVersion")
    api("com.github.only52607.luakt:luakt-extension:$luaKTVersion")
    api("com.github.only52607.luakt:luakt-luaj:$luaKTVersion")
    library("cloud.commandframework:cloud-paper:$cloudVersion")
    library("cloud.commandframework:cloud-brigadier:$cloudVersion")
    library("cloud.commandframework:cloud-annotations:$cloudVersion")
}

paper {
    loader = "xyz.galaxyy.lualink.PluginLibrariesLoader"
    main = "xyz.galaxyy.lualink.LuaLink"
    name = "LuaLink"
    authors = listOf("Element4521")
    description = "A plugin that allows you to run Lua scripts in Minecraft."
    apiVersion = "1.20"

    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    generateLibrariesJson = true
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("lualink")
    versionNumber.set(version.toString())
    versionType.set("release")
    uploadFile.set(tasks.jar.get())
    gameVersions.addAll("1.20.1", "1.20.2", "1.20.3", "1.20.4")
    loaders.addAll("paper", "purpur")
    changelog.set(System.getenv("GIT_COMMIT_MESSAGE"))
}

hangarPublish {
    publications.register("plugin") {
        version.set(project.version as String) // use project version as publication version
        id.set("LuaLink")
        channel.set("Release")
        changelog.set(System.getenv("GIT_COMMIT_MESSAGE")) // optional

        apiKey.set(System.getenv("HANGAR_API_KEY"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.20.1", "1.20.2", "1.20.3", "1.20.4"))
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = "xyz.galaxyy.lualink"
            artifactId = "lualink"
            version = project.version as String

            from(components["java"])

            versionMapping {
                usage("java-api") { fromResolutionOf("runtimeClasspath") }
                usage("java-runtime") { fromResolutionResult() }
            }

            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/LuaLink/LuaLink")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}

tasks.test { useJUnitPlatform() }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "17" }

application { mainClass.set("MainKt") }
