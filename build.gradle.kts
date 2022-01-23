plugins {
    kotlin("multiplatform") version "1.6.10"
    id("maven-publish")
}

group = "net.arwix.urania"
version = "1.0-alpha05"

repositories {
    mavenCentral()
    mavenLocal()
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
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
                implementation ("net.arwix.urania:astronomy-vsop87a:0.0.2")
                implementation ("net.arwix.urania:astronomy-moshier:1.0-alpha07")
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
