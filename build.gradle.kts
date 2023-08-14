import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import java.time.Instant
import java.time.format.DateTimeFormatter

fun property(key: String) = project.findProperty(key).toString()
fun optionalProperty(key: String) = project.findProperty(key)?.toString()

apply(from = "https://raw.githubusercontent.com/SizableShrimp/Forge-Class-Remapper/main/classremapper.gradle")
apply(from = "https://gist.githubusercontent.com/Harleyoc1/4d23d4e991e868d98d548ac55832381e/raw/applesiliconfg.gradle")

plugins {
    id("java")
    id("net.minecraftforge.gradle")
    id("org.parchmentmc.librarian.forgegradle")
    id("idea")
    id("maven-publish")
    id("com.harleyoconnor.translationsheet") version "0.1.1"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.+"
    id("com.harleyoconnor.autoupdatetool") version "1.0.9"
}

repositories {
    maven("https://ldtteam.jfrog.io/ldtteam/modding/")
    maven("https://maven.tehnut.info")
    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
    maven("https://harleyoconnor.com/maven")
    maven("https://squiddev.cc/maven/")
}

val modName = property("modName")
val modId = property("modId")
val modVersion = property("modVersion")
val mcVersion = property("mcVersion")

version = "$mcVersion-$modVersion"
group = property("group")

minecraft {
    mappings("parchment", "${property("mappingsVersion")}-$mcVersion")
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        create("client") {
            applyDefaultConfiguration()

            if (project.hasProperty("mcUuid")) {
                args("--uuid", property("mcUuid"))
            }
            if (project.hasProperty("mcUsername")) {
                args("--username", property("mcUsername"))
            }
            if (project.hasProperty("mcAccessToken")) {
                args("--accessToken", property("mcAccessToken"))
            }
        }

        create("server") {
            applyDefaultConfiguration("run-server")
        }

        create("data") {
            applyDefaultConfiguration()

            args(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources/"),
                "--existing", file("src/main/resources")
            )
        }
    }
}

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
    srcDir("src/localization/resources")
}

dependencies {
    minecraft("net.minecraftforge:forge:$mcVersion-${property("forgeVersion")}")

    implementation(fg.deobf("curse.maven:jade-324717:4433884"))

    compileOnly(fg.deobf("mezz.jei:jei-$mcVersion-forge-api:${property("jeiVersion")}"))
    compileOnly(fg.deobf("mezz.jei:jei-$mcVersion-common-api:${property("jeiVersion")}"))
    runtimeOnly(fg.deobf("mezz.jei:jei-$mcVersion-forge:${property("jeiVersion")}"))

    implementation(fg.deobf("curse.maven:SereneSeasons-291874:${property("ssVersion")}"))

    runtimeOnly(fg.deobf("vazkii.patchouli:Patchouli:${property("patchouliVersion")}"))
    runtimeOnly(fg.deobf("org.squiddev:cc-tweaked-$mcVersion:${property("ccVersion")}"))
    runtimeOnly(fg.deobf("com.harleyoconnor.suggestionproviderfix:SuggestionProviderFix-1.19:${property("suggestionProviderFixVersion")}"))
}

translationSheet {
    sheetId.set("1xjxEh2NdbeV_tQc6fDHPgcRmtchqCZJKt--6oifq1qc")
    sectionColour.set(0xF9CB9C)
    sectionPattern.set("Dynamic Trees")
    outputDirectory.set(file("src/localization/resources/assets/dynamictrees/lang/"))
    useJson()
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val changelogFile = file("build/changelog.txt")

curseforge {
    val curseApiKey = optionalProperty("curseApiKey") ?: System.getenv("CURSEFORGE_API_KEY")
    if (curseApiKey == null) {
        project.logger.warn("API Key for CurseForge not detected; uploading will be disabled.")
        return@curseforge
    }

    apiKey = curseApiKey

    project {
        id = "252818"

        addGameVersion(mcVersion)

        changelog = changelogFile
        changelogType = "markdown"
        releaseType = optionalProperty("versionType") ?: "release"

        addArtifact(tasks.findByName("sourcesJar"))

        mainArtifact(tasks.findByName("jar")) {
            relations {
                optionalDependency("dynamictreesplus")
            }
        }
    }
}

modrinth {
    val modrinthToken = optionalProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN")
    if (modrinthToken == null) {
        project.logger.warn("Token for Modrinth not detected; uploading will be disabled.")
        return@modrinth
    }

    token.set(modrinthToken)
    projectId.set(modId)
    versionNumber.set("$mcVersion-$modVersion")
    versionType.set(optionalProperty("versionType") ?: "release")
    uploadFile.set(tasks.jar.get())
    gameVersions.add(mcVersion)
    if (changelogFile.exists()) {
        changelog.set(changelogFile.readText())
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
        val mavenUsername = optionalProperty("harleyOConnorMavenUsername") ?: System.getenv("MAVEN_USERNAME")
        val mavenPassword = optionalProperty("harleyOConnorMavenPassword") ?: System.getenv("MAVEN_PASSWORD")
        if (mavenUsername == null || mavenPassword == null) {
            logger.warn("Credentials for maven not detected; it will be disabled.")
            return@repositories
        }
        maven("https://harleyoconnor.com/maven") {
            name = "HarleyOConnor"
            credentials {
                username = mavenUsername
                password = mavenPassword
            }
        }
    }
}

autoUpdateTool {
    minecraftVersion.set(mcVersion)
    version.set(modVersion)
    versionRecommended.set(property("versionRecommended") == "true")
    changelogOutputFile.set(changelogFile)
    updateCheckerFile.set(file("build/version_info.json"))
}

tasks.autoUpdate {
    doLast {
        modrinth.changelog.set(changelogFile.readText())
    }
    finalizedBy("publishMavenJavaPublicationToHarleyOConnorRepository", "curseforge", "modrinth")
}

fun net.minecraftforge.gradle.common.util.RunConfig.applyDefaultConfiguration(runDirectory: String = "run") {
    workingDirectory = file(runDirectory).absolutePath

    property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
    property("forge.logging.console.level", "debug")

    property("mixin.env.remapRefMap", "true")
    property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")

    mods {
        create(modId) {
            source(sourceSets.main.get())
        }
    }
}

fun com.matthewprenger.cursegradle.CurseExtension.project(action: CurseProject.() -> Unit) {
    this.project(closureOf(action))
}

fun CurseProject.mainArtifact(artifact: Task?, action: CurseArtifact.() -> Unit) {
    this.mainArtifact(artifact, closureOf(action))
}

fun CurseArtifact.relations(action: CurseRelation.() -> Unit) {
    this.relations(closureOf(action))
}
