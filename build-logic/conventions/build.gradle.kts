import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("lokalizeConvention") {
            id = "org.multipaz.lokalize.convention"
            implementationClass = "org.multipaz.lokalize.convention.LokalizeConventionPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.android.tools.gradle.plugin)

    implementation(project(":lokalize"))
}
