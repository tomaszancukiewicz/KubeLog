import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("antlr")
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
    kotlin("plugin.allopen") version "1.4.31"
    kotlin("plugin.jpa") version "1.4.31"
    kotlin("plugin.spring") version "1.4.31"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.4.4"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = "15.0.1"
    modules("javafx.controls", "javafx.fxml")
}

group = "com.payu.kube.log"
version = "1.3.1"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-web")

    antlr("org.antlr:antlr4:4.9.2")

    implementation("com.github.Dansoftowner:jSystemThemeDetector:2.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    buildInfo()
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    dependsOn(tasks.generateGrammarSource)
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.register("version") {
    println(version)
}

tasks.register("creatAppBundle") {
    group = "build"
    dependsOn(tasks.withType<BootJar>())
    doLast {
        val buildAppDir = buildDir.toPath().resolve("app")
        println("Create app bundle in: $buildAppDir")
        delete(buildAppDir)
        mkdir(buildAppDir)

        val appDir = buildAppDir.resolve("${project.name}.app")
        val contentDir = appDir.resolve("Contents")

        val appSrc = project.projectDir.resolve("src/main/app")
        copy {
            from(appSrc.resolve("Contents"))
            into(contentDir)
        }

        val macOsDir = contentDir.resolve("MacOS")
        val bootJarTask = project.tasks.withType<BootJar>()
        val jarFile = bootJarTask.map { it.outputs.files.singleFile }.first()
        copy {
            from(jarFile)
            into(macOsDir)
            rename(jarFile.name, "KubeLog.jar")
        }
        exec {
            workingDir(macOsDir)
            commandLine("chmod", "+x", "./run_kubelog")
        }

        val entitlements = appSrc.resolve("java.entitlements")
        exec {
            workingDir(buildAppDir)
            commandLine("codesign", "-f", "--deep",
                "--entitlements", entitlements,
                "-s", "Code Signing Certificate", appDir)
        }

        val zipDir = buildAppDir.resolve("${project.name}.zip")
        exec {
            workingDir(buildAppDir)
            commandLine("zip", "-r", zipDir, appDir.fileName)
        }
    }
}