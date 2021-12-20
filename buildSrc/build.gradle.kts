plugins {
    `kotlin-dsl`
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://files.minecraftforge.net/maven")
    maven("https://gitlab.com/api/v4/projects/26758973/packages/maven")
}

dependencies {
    implementation("net.minecraftforge.gradle:ForgeGradle:5.1.+")
    implementation("com.google.code.gson:gson:2.8.7")
}
