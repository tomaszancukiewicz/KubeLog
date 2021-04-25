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
version = "1.3.7"

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
        val srcAppDir = project.projectDir.resolve("src/main/app")
        delete(temporaryDir)
        val tempDstDir = temporaryDir.toPath().resolve("dst")
        mkdir(tempDstDir)

        // create app
        val tempAppDir = tempDstDir.resolve("${project.name}.app")
        val contentsDir = tempAppDir.resolve("Contents")

        copy {
            from(srcAppDir.resolve("Contents"))
            into(contentsDir)
        }

        val macOsDir = contentsDir.resolve("MacOS")
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
        exec {
            workingDir(tempDstDir)
            commandLine("codesign", "-f", "--deep",
                "--entitlements", srcAppDir.resolve("java.entitlements"),
                "-s", "Code Signing Certificate", tempAppDir)
        }

        // create temp pkg
        val tempPkgDir = temporaryDir.resolve("temp.pkg")
        exec {
            workingDir(temporaryDir)
            commandLine("pkgbuild",
                "--install-location", "/Applications",
                "--identifier", "com.payu.KubeLog",
                "--root", tempDstDir,
                "--component-plist", srcAppDir.resolve("component.plist"),
                tempPkgDir)
        }

        // output path
        val buildAppDir = buildDir.toPath().resolve("app")
        delete(buildAppDir)
        mkdir(buildAppDir)

        // copy app
        copy {
            from(tempAppDir) {
                into("KubeLog.app")
            }
            into(buildAppDir)
        }

        // create pkg
        exec {
            workingDir(buildAppDir)
            commandLine("productbuild",
                "--package", tempPkgDir,
                "--product", srcAppDir.resolve("requirements.plist"),
                "--cert", "Code Signing Certificate",
                buildAppDir.resolve("${project.name}.pkg"))
        }
    }
}