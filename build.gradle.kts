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
    id("com.harleyoconnor.autoupdatetool") version "1.0.0"
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
            applyDefaultConfiguration()
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

    implementation(fg.deobf("curse.maven:jade-324717:3970956"))

    compileOnly(fg.deobf("mezz.jei:jei-$mcVersion:${property("jeiVersion")}:api"))
    runtimeOnly(fg.deobf("mezz.jei:jei-$mcVersion:${property("jeiVersion")}"))

    implementation(fg.deobf("curse.maven:SereneSeasons-291874:3693807"))

    runtimeOnly(fg.deobf("vazkii.patchouli:Patchouli:${property("patchouliVersion")}"))
    runtimeOnly(fg.deobf("org.squiddev:cc-tweaked-$mcVersion:${property("ccVersion")}"))
    runtimeOnly(fg.deobf("com.harleyoconnor.suggestionproviderfix:SuggestionProviderFix-1.18.1:${property("suggestionProviderFixVersion")}"))
}

translationSheet {
    this.sheetId.set("1xjxEh2NdbeV_tQc6fDHPgcRmtchqCZJKt--6oifq1qc")
    this.sectionColour.set(0xF9CB9C)
    this.sectionPattern.set("Dynamic Trees")
    this.outputDir("src/localization/resources/assets/dynamictrees/lang/")

    this.useJson()
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

curseforge {
    if (project.hasProperty("curseApiKey") && project.hasProperty("curseFileType")) {
        apiKey = property("curseApiKey")

        project {
            id = "252818"

            addGameVersion(mcVersion)

            changelog = file("changelog.txt")
            changelogType = "markdown"
            releaseType = property("curseFileType")

            addArtifact(tasks.findByName("sourcesJar"))

            mainArtifact(tasks.findByName("jar")) {
                relations {
                    optionalDependency("dynamictreesplus")
                    optionalDependency("chunk-saving-fix")
                }
            }
        }
    } else {
        project.logger.log(
            LogLevel.WARN,
            "API Key and file type for CurseForge not detected; uploading will be disabled."
        )
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
        if (hasProperty("harleyOConnorMavenUsername") && hasProperty("harleyOConnorMavenPassword")) {
            maven("https://harleyoconnor.com/maven") {
                name = "HarleyOConnor"
                credentials {
                    username = property("harleyOConnorMavenUsername")
                    password = property("harleyOConnorMavenPassword")
                }
            }
        } else {
            logger.log(LogLevel.WARN, "Credentials for maven not detected; it will be disabled.")
        }
    }
}

tasks.register("publishToAllPlatforms") {
    this.dependsOn("publishMavenJavaPublicationToHarleyOConnorRepository", "curseforge")
}

autoUpdateTool {
    this.mcVersion.set(mcVersion)
    this.version.set(modVersion)
    this.versionRecommended.set(property("versionRecommended") == "true")
    this.updateCheckerFile.set(file(property("dynamictrees.version_info_repo.path") + "/" + property("updateCheckerPath")))
}

tasks.autoUpdate {
    finalizedBy("publishMavenJavaPublicationToHarleyOConnorRepository", "curseforge")
}

//val versionInfoRepoPath = property("dynamictrees.version_info_repo.path")
//val updateCheckerPath = property("updateCheckerPath")
//val versionRecommended = property("versionRecommended") == "true"
//val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
//
//tasks.register("publishUpdate") {
//    doFirst {
//        val changelog = buildChangelog().joinToString("\n")
//        if (project.hasProperty("updateCheckerPath")) {
//            updateVersionInfo(changelog)
//        }
//        writeToChangelogFile(changelog)
//
//        // Tag version
//        executeGitCommand(listOf("git", "tag", modVersion))
//        executeGitCommand(listOf("git", "push", "--tags"))
//    }
//    finalizedBy("publishMavenJavaPublicationToHarleyOConnorRepository", "curseforge")
//}
//
//fun buildChangelog(): List<String> {
//    val lastVersion = executeGitCommand(listOf("git", "describe", "--tags", "--abbrev=0")).trim()
//    return executeGitCommand(listOf("git", "log", "$lastVersion..HEAD", "--oneline")).split("\n")
//        .stream()
//        .filter { it.trim().isNotEmpty() }
//        .map { commitData -> commitData.substring(0, commitData.indexOf(' ')) }
//        .map { hash -> getCommit(hash) }
//        .map { commit -> commit.formatInChangelog() }
//        .toList()
//}
//
//fun getCommit(hash: String): Commit {
//    val commitData = executeGitCommand(listOf("git", "show", hash)).split("\n").toMutableList()
//    if (commitData[1].startsWith("Merge")) {
//        commitData.removeAt(1)
//    }
//    val authorName = getCommitAuthorName(commitData[1])
//    val message = commitData[4].trim()
//    return Commit(hash, authorName, message)
//}
//
//fun getCommitAuthorName(authorData: String): String {
//    val startOfEmail = authorData.indexOf("<")
//    val endIndex = if (startOfEmail < 0) authorData.length else startOfEmail - 1
//    return authorData.substring(authorData.indexOf(":") + 2, endIndex)
//}
//
//data class Commit(
//    val hash: String,
//    val authorName: String,
//    val message: String
//) {
//
//    fun formatInChangelog(): String {
//        return "- $message [$authorName]"
//    }
//
//}
//
//fun updateVersionInfo(changelog: String) {
//    val updateCheckerFile = file("$versionInfoRepoPath/$updateCheckerPath")
//    val json = gson.fromJson(updateCheckerFile.readText(), JsonElement::class.java).asJsonObject(
//        "Update checker Json invalid: root element must be an object."
//    )
//    updateVersionInfoJson(json, changelog)
//    writeToVersionInfoFile(json, updateCheckerFile)
//    commitAndPushChangesToVersionInfoFile()
//}
//
//fun updateVersionInfoJson(json: JsonObject, changelog: String) {
//    val changelogJson = json.getOrCreateJsonObject(
//        mcVersion,
//        "Update check Json invalid: \"$mcVersion\" property must be an object."
//    )
//    // Update version changelog with pre-built changelog
//    changelogJson.addProperty("$mcVersion-$modVersion", changelog)
//
//    val promosJson = json.getOrCreateJsonObject(
//        "promos",
//        "Update check Json invalid: \"promos\" property must be an object."
//    )
//    // Update promos Json with new version
//    promosJson.addProperty("$mcVersion-latest", "$mcVersion-$modVersion")
//    if (versionRecommended) {
//        promosJson.addProperty("$mcVersion-recommended", "$mcVersion-$modVersion")
//    }
//}
//
//fun writeToVersionInfoFile(json: JsonObject, updateCheckerFile: File) {
//    try {
//        writeJsonToFile(json, updateCheckerFile)
//    } catch (e: Exception) {
//        error("Error writing update checker file: " + e.message.toString())
//    }
//}
//
//fun writeJsonToFile(json: JsonElement, file: File) {
//    writeTextToFile(getJsonAsString(json), file)
//}
//
//fun getJsonAsString(json: JsonElement): String {
//    val writer = StringWriter()
//    gson.toJson(json, writer)
//    return writer.toString()
//}
//
///**
// * Writes to `changelog.txt`, which is read by the CurseGradle task for publishing to CurseForge.
// */
//fun writeToChangelogFile(changelog: String) {
//    try {
//        writeTextToFile(changelog, file("changelog.txt"))
//    } catch (e: Exception) {
//        error("Error writing changelog file: " + e.message.toString())
//    }
//}
//
//fun writeTextToFile(contents: String, file: File) {
//    val writer = FileWriter(file)
//    writer.write(contents)
//    writer.close()
//}
//
//fun commitAndPushChangesToVersionInfoFile() {
//    // Add, commit, and push changes in version info repo
//    val versionInfoRepoDir = file(versionInfoRepoPath)
//    executeGitCommand(listOf("git", "add", updateCheckerPath), versionInfoRepoDir)
//    executeGitCommand(listOf("git", "commit", "-m", "Update version info for $modName"), versionInfoRepoDir)
//    executeGitCommand(listOf("git", "push"), versionInfoRepoDir)
//}
//
///**
// * @param errorMessage message to display if this json element is not a json object
// */
//fun JsonElement.asJsonObject(errorMessage: String): JsonObject {
//    if (this.isJsonObject) {
//        return this.asJsonObject
//    } else {
//        error(errorMessage)
//    }
//}
//
///**
// * @param errorMessage message to display if the element with the specified key exists and is not a json object
// */
//fun JsonObject.getOrCreateJsonObject(key: String, errorMessage: String): JsonObject {
//    val json: JsonObject
//    if (this.has(key)) {
//        if (!this.get(key).isJsonObject) {
//            error(errorMessage)
//        }
//        json = this.getAsJsonObject(key)
//    } else {
//        json = JsonObject()
//        this.add(key, json)
//    }
//    return json
//}
//
///**
// * @return standard output from the process
// */
//fun executeGitCommand(command: List<String>, workingDir: File? = null): String {
//    try {
//        val processBuilder = ProcessBuilder().command(command)
//        if (workingDir != null) {
//            processBuilder.directory(workingDir)
//        }
//        val process = processBuilder.start()
//        val out = process.inputStream.reader().readText()
//        process.waitFor()
//        if (process.exitValue() != 0) {
//            error(process.errorStream.reader().readText())
//        }
//        return out
//    } catch (e: Exception) {
//        error("Exception while executing Git command `$command`: " + e.message.toString())
//    }
//}

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
