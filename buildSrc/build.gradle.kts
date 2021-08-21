plugins {
    `kotlin-dsl`
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://files.minecraftforge.net/maven")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("net.minecraftforge.gradle:ForgeGradle:5.1.+")
    implementation("org.parchmentmc:librarian:1.+")
}