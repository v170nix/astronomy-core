plugins {
    kotlin("multiplatform") version "1.6.10"
}

group = "net.arwix.urania"
version = "0.0.1-SNAPSHOT"

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
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
//                implementation(npm("@js-joda/timezone", "2.3.0"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}
