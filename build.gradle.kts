plugins {
    kotlin("jvm") version "1.9.21"
    id("org.openjfx.javafxplugin") version "0.1.0"

}

javafx {
    version = "23.0.2"
    modules = mutableListOf("javafx.controls")
}

group = "agh.edu.pl.backend"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}