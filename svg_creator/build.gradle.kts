plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application

}

val mainPackage = "com.zelgius.laserCut.svg_generator"
group = mainPackage
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("batik:batik-svggen:1.6-1")
    implementation("commons-cli:commons-cli:1.5.0")
    implementation("org.apache.xmlgraphics:batik-dom:1.16")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}


application {
    mainClass.set("${mainPackage}.MainKt")
}