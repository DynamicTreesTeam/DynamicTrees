import com.google.gson.Gson
import com.google.gson.JsonObject
import com.matthewprenger.cursegradle.*
import org.apache.tools.ant.filters.ReplaceTokens
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    id("java")
    id("net.minecraftforge.gradle")
    id("idea")
    id("maven-publish")
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

repositories {
    maven("https://maven.tehnut.info")
    maven("https://www.cursemaven.com")
    maven("https://maven.blamejared.com")
}

val buildProperties = Properties()
buildProperties.load(FileInputStream(file("build.properties")))

fun property(key: String) =
    buildProperties.getProperty(key)

val modName = property("modName")
val modId = property("modId")
val modVersion = property("modVersion")

val mcVersion = property("mcVersion")
val forgeVersion = property("forgeVersion")
val mappingsVersion = property("mappingsVersion")

version = "$mcVersion-$modVersion"
group = property("group")

minecraft {
    mappings("snapshot", mappingsVersion)

    runs {
        create("client") {
            workingDirectory = file("run").absolutePath

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")

            if (project.hasProperty("mcUuid")) {
                args("--uuid", project.property("mcUuid"))
            }
            if (project.hasProperty("mcUsername")) {
                args("--username", project.property("mcUsername"))
            }
            if (project.hasProperty("mcAccessToken")) {
                args("--accessToken", project.property("mcAccessToken"))
            }
        }

        create("server") {
            workingDirectory = file("run").absolutePath

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
        }
    }
}

dependencies {
    // Not sure if we need this one, what is a "forge" anyway?
    minecraft("net.minecraftforge:forge:${mcVersion}-${forgeVersion}")

    // Compile against full Hwyla jar.
    implementation("mcp.mobius.waila:Hwyla:1.8.26-B41_1.12.2")

    // Compile against Serene Seasons, but don't use at runtime.
    compileOnly("curse.maven:SereneSeasons-291874:2799213")

    // Use Patchouli at runtime only (for testing the guide book, which is exclusively Json).
    runtimeOnly("vazkii.patchouli:Patchouli:1.0-19.96")
}

// Workaround for resources issue. Use gradle tasks rather than generated runs for now.
sourceSets {
    main {
        output.setResourcesDir(file("build/combined"))
        java.destinationDirectory.set(file("build/combined"))
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)

    from(sourceSets.main.get().resources.srcDirs) {
        include("mcmod.info")

        expand("version" to project.version, "mcversion" to mcVersion)
    }

    from(sourceSets.main.get().resources.srcDirs) {
        exclude("mcmod.info")
    }
}

// Assign version constant in ModConstants.
val prepareSources = tasks.register("prepareSources", Copy::class) {
    from("src/main/java")
    into("build/src/main/java")
    filter<ReplaceTokens>("tokens" to mapOf("VERSION" to version.toString()))
}

tasks.compileJava {
    source = prepareSources.get().outputs.files.asFileTree
}

tasks.jar {
    manifest.attributes(
        "Specification-Title" to project.name,
        "Specification-Vendor" to "ferreusveritas",
        "Specification-Version" to "1",
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "ferreusveritas",
        "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    )

    archiveBaseName.set(modName)
    finalizedBy("reobfJar")
}

java {
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val deobfJar = tasks.register("deobfJar", Jar::class) {
    archiveClassifier.set("deobf")
    from(sourceSets.main.get().output)
}

tasks.build {
    dependsOn(deobfJar)
}

fun readChangelog(): String? {
    val versionInfoFile = file("version_info.json")
    val jsonObject = Gson().fromJson(InputStreamReader(versionInfoFile.inputStream()), JsonObject::class.java)
    return jsonObject
        .get(mcVersion)?.asJsonObject
        ?.get(project.version.toString())?.asString
}

curseforge {
    if (project.hasProperty("curseApiKey")) {
        apiKey = project.property("curseApiKey")

        project {
            id = "252818"

            addGameVersion(mcVersion)

            changelog = readChangelog() ?: "No changelog provided."
            changelogType = "markdown"
            releaseType = property("curseFileType") ?: "release"

            addArtifact(tasks.findByName("sourcesJar"))
            addArtifact(deobfJar.get())
        }
    } else {
        project.logger.log(LogLevel.WARN, "API Key and file type for CurseForge not detected; uploading will be disabled.")
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "$modName-$mcVersion"
            version = modVersion

            from(components["java"])
            artifact(deobfJar.get())

            pom {
                name.set(modName)
                url.set("https://github.com/ferreusveritas/$modName")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org")
                    }
                }
                developers {
                    developer {
                        id.set("ferreusveritas")
                        name.set("Ferreus Veritas")
                    }
                    developer {
                        id.set("supermassimo")
                        name.set("Max Hyper")
                    }
                    developer {
                        id.set("Harleyoc1")
                        name.set("Harley O'Connor")
                        email.set("Harleyoc1@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ferreusveritas/$modName.git")
                    developerConnection.set("scm:git:ssh://github.com/ferreusveritas/$modName.git")
                    url.set("https://github.com/ferreusveritas/$modName")
                }
            }

            pom.withXml {
                val element = asElement()

                // Clear dependencies.
                for (i in 0 until element.childNodes.length) {
                    val node = element.childNodes.item(i)
                    if (node?.nodeName == "dependencies") {
                        element.removeChild(node)
                    }
                }
            }
        }
    }
    repositories {
        maven("file:///${project.projectDir}/mcmodsrepo")
        if (project.hasProperty("harleyOConnorMavenUsername") &&
            project.hasProperty("harleyOConnorMavenPassword")) {
            maven("https://harleyoconnor.com/maven") {
                name = "HarleyOConnor"
                credentials {
                    username = project.property("harleyOConnorMavenUsername").toString()
                    password = project.property("harleyOConnorMavenPassword").toString()
                }
            }
        } else {
            logger.log(LogLevel.WARN, "Credentials for maven not detected; it will be disabled.")
        }
    }
}

// Extensions to make CurseGradle extension slightly neater.

fun CurseExtension.project(action: CurseProject.() -> Unit) {
    this.project(closureOf(action))
}

fun CurseProject.mainArtifact(artifact: Task?, action: CurseArtifact.() -> Unit) {
    this.mainArtifact(artifact, closureOf(action))
}

fun CurseArtifact.relations(action: CurseRelation.() -> Unit) {
    this.relations(closureOf(action))
}
