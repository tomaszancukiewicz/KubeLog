import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.buildinfo.BuildInfo
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("antlr")
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.springframework.boot") version "2.6.2"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "com.payu.kube.log"
version = "1.4.0"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    jcenter()
    mavenCentral()
    google()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("ch.qos.logback:logback-classic:1.2.10")

    antlr("org.antlr:antlr4:4.9.3")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.components:components-splitpane:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("io.ktor:ktor-client-core:1.6.7")
    implementation("io.ktor:ktor-client-cio:1.6.7")
    implementation("io.ktor:ktor-client-serialization:1.6.7")
}

compose.desktop {
    application {
        mainClass = "com.payu.kube.log.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
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