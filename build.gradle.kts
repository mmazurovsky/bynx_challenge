plugins {
    kotlin("jvm") version "2.1.0"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.bynx.anagrams.AnagramsApplicationKt")
}

dependencies {
    testImplementation(kotlin("test"))
    // Redundant on Gradle 8, required from Gradle 9 on.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
