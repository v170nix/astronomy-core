import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    kotlin("multiplatform") version "1.8.0"
    id("maven-publish")
}

group = "net.arwix.urania"
version = "1.0.0-alpha23"

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://jitpack.io")
}
//dependencies {
//    implementation("org.testng:testng:7.1.0")
//}
//dependencies {
//    implementation("junit:junit:4.13.1")
//}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
//        moduleName = "@astronomy/core"
        nodejs() {

        }
        browser()
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
//                implementation ("net.arwix.urania:astronomy-vsop87a:0.0.2")
                implementation ("net.arwix.urania:astronomy-moshier:1.0.0-alpha22")
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
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


rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
// https://kotlinlang.org/docs/whatsnew18.html#new-settings-for-reporting-that-yarn-lock-has-been-updated
//    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
//        YarnLockMismatchReport.WARNING // NONE | FAIL
//    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true // true
}
