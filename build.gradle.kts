import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.buildinfo.BuildInfo
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("antlr")
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("org.springframework.boot") version "2.7.18"
    id("org.jetbrains.compose") version "1.6.2"
}

group = "com.payu.kube.log"
version = "1.5.3"

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("ch.qos.logback:logback-classic:1.5.5")

    antlr("org.antlr:antlr4:4.13.1")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(compose.desktop.macos_x64)
    implementation(compose.desktop.macos_arm64)
    implementation(compose.material3)
    implementation("org.jetbrains.compose.components:components-splitpane:1.5.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.ktor:ktor-client-core:2.3.10")
    implementation("io.ktor:ktor-client-cio:2.3.10")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
}

compose.desktop {
    application {
        mainClass = "com.payu.kube.log.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = project.name
            packageVersion = project.version.toString()
        }
    }
}

springBoot {
    mainClass.set("com.payu.kube.log.MainKt")
    buildInfo()
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages", "-package", "com.payu.kube.log.search.query")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    dependsOn(tasks.withType<BuildInfo>())
    dependsOn(tasks.generateGrammarSource)
    dependsOn(tasks.generateTestGrammarSource)
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
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
        val buildAppDir = layout.buildDirectory.dir("app").get().asFile
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