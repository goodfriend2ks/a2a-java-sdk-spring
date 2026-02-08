plugins {
    id("java")
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.timo.ai.a2a"
version = "1.0-SNAPSHOT"
description = "A2A Java SDK for Spring Framework"

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.1.2"
extra["springGrpcVersion"] = "0.12.0"
// extra["a2aSdkVersion"] = "0.3.3.Final"
extra["a2aSdkVersion"] = "1.0.0.Alpha1"

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        implementation("io.github.a2asdk:a2a-java-sdk-spec:${property("a2aSdkVersion")}")
        implementation("io.github.a2asdk:a2a-java-sdk-common:${property("a2aSdkVersion")}")

        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.test {
    useJUnitPlatform()
}
