plugins {
    id("java")
    id("io.freefair.lombok") version "9.2.0"
}

val assertJVersion = "3.27.7"
val lombokPluginVersion = "9.2.0"
val junitVersion = "5.10.0"
val logbackVersion = "1.5.32"
group = "com.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:$lombokPluginVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.googlecode.lanterna:lanterna:3.1.2")
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
