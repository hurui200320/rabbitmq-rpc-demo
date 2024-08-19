import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.10"
    id("com.google.devtools.ksp") version "2.0.10-1.0.24"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("io.projectreactor.rabbitmq:reactor-rabbitmq:1.5.6")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    implementation(project(":generator"))
    ksp(project(":generator"))

    testImplementation(kotlin("test"))
}

allprojects {
    apply(plugin = "kotlin")

    group = "info.skyblond"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.test {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
