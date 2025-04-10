plugins {
    kotlin("jvm") version "2.0.21"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "win.templeos.lualink"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val luaJavaVersion:String by project

dependencies {
    // Kotlin (downloaded and loaded at runtime)
    paperLibrary(kotlin("stdlib"))
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    // LuaJava (cannot be easily relocated or downloaded at runtime)
    implementation("party.iroiro.luajava:luajava:4.0.3-SNAPSHOT")
    implementation("party.iroiro.luajava:luajit:$luaJavaVersion")
    // LuaJava natives (cannot be easily relocated or downloaded at runtime)
    runtimeOnly("party.iroiro.luajava:luajit-platform:$luaJavaVersion:natives-desktop")
}

paper {
    main = "win.templeos.lualink.LuaLink"
    apiVersion = "1.20"
    generateLibrariesJson = true
    authors = listOf("Saturn745")
    loader = "win.templeos.lualink.PluginLibrariesLoader"
    // This allows the plugin to access external plugins' APIs without depending on them.
    // This may cause conflicts if different versions of the same dependency are loaded by different plugins.
    hasOpenClassloader = true
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
    }
}

tasks.test {
    useJUnitPlatform()
}