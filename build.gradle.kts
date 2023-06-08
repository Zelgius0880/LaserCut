import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

val mainPackage = "com.zelgius.laserCut"
group = mainPackage
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val pi4jVersion = "2.1.0"

    implementation("com.pi4j:pi4j-core:$pi4jVersion")
    implementation(group = "com.pi4j", name = "pi4j-plugin-linuxfs", version = pi4jVersion)
    implementation(group = "com.pi4j", name = "pi4j-plugin-pigpio", version = pi4jVersion)
    implementation(group = "com.pi4j", name = "pi4j-plugin-raspberrypi", version = pi4jVersion)
    implementation("org.json:json:20230227")
    implementation ("io.socket:socket.io-client:2.1.0") {
        // excluding org.json which is provided by Android
        exclude (group= "org.json", module= "json")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("${mainPackage}.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}



lateinit var archiveJar: Jar
tasks {
    named<ShadowJar>("shadowJar") {
        archiveJar = this
        manifest {
            attributes(
                "Implementation-Version" to archiveVersion.get(),
                "Main-Class" to "$mainPackage.MainKt"
            )
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set(project.name)
        mergeServiceFiles()
    }
}

setupJavaDeployTasks( tasks.shadowJar.get().archiveFile.get().asFile, tasks.shadowJar)