plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

group = "dev.tildejustin"
version = "1.0"

repositories {
    mavenCentral()
    maven { setUrl("https://maven.fabricmc.net/") }
}

dependencies {
    implementation("net.fabricmc:mapping-io:0.5.1")
    implementation("net.fabricmc:mercury:0.4.1")
    implementation("com.google.guava:guava:33.0.0-jre")
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        this.groupId = "dev.tildejustin"
        this.artifactId = "yarn"
        this.version = "1.12.2+build.202206171821-1.12.2+build.536-translation"
        this.artifact(file("1.12.2+build.202206171821-1.12.2+build.536-translation.jar"))
    }
    repositories {
        mavenLocal()
    }
}
