plugins {
    kotlin("multiplatform") version "1.6.10"
    id("maven-publish")
}

group = "net.arwix.urania"
version = "0.0.2-alpha01"

repositories {
    mavenCentral()
}
//dependencies {
//    implementation("junit:junit:4.13.1")
//}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
//        binaries.library()
        nodejs {

        }
        compilations.all {
//            kotlinOptions {
//                sourceMap = true
//                moduleKind = "umd"
//                metaInfo = true
//            }
//            compileKotlinTask.kotlinOptions.freeCompilerArgs += listOf("-Xerror-tolerance-policy=SYNTAX")
        }
        binaries.library()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
//                implementation(npm("@js-joda/timezone", "2.3.0"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

publishing {
    repositories {
        maven {
            url = uri("file://${System.getenv("HOME")}/.m2/repository")
        }
    }
}

if (System.getenv("JITPACK") == "true")
    tasks["publishToMavenLocal"].doLast {
        val version = System.getenv("VERSION")
        val artifacts = publishing.publications.filterIsInstance<MavenPublication>().map { it.artifactId }

        val dir: File = File(publishing.repositories.mavenLocal().url)
            .resolve(project.group.toString().replace('.', '/'))

        dir.listFiles { it -> it.name in artifacts }
            .flatMap {
                (
                        it.listFiles { it -> it.isDirectory }?.toList()
                            ?: emptyList<File>()
                        ) + it.resolve("maven-metadata-local.xml")
            }
            .flatMap {
                if (it.isDirectory) {
                    it.listFiles { it ->
                        it.extension == "module" ||
                                "maven-metadata" in it.name ||
                                it.extension == "pom"
                    }?.toList() ?: emptyList()
                } else listOf(it)
            }
            .forEach {
                val text = it.readText()
                println("Replacing ${project.version} with $version in $it")
                it.writeText(text.replace(project.version.toString(), version))
            }
    }