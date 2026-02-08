plugins {
    id("java")
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"

    id("maven-publish")
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

    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencies {
        implementation("io.github.a2asdk:a2a-java-sdk-spec:${property("a2aSdkVersion")}")
        implementation("io.github.a2asdk:a2a-java-sdk-common:${property("a2aSdkVersion")}")

        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    /*
    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }

    tasks.getByName<org.gradle.api.tasks.bundling.Jar>("jar") {
        enabled = true
    }
    */
    // or a more concise way
    tasks {
        bootJar {
            enabled = false
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

tasks {
    bootJar {
        enabled = false
    }
}

publishing {
    repositories {
        /**
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        */
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/goodfriend2ks/a2a-java-sdk-spring")
            credentials {
                username = findProperty("gpr.user") as String?
                    ?: System.getenv("USERNAME_GITHUB")
                password = findProperty("gpr.token") as String?
                    ?: System.getenv("TOKEN_GITHUB")
            }
        }
    }
    /**
    // Configure for Kotlin Multiplatform library
    publications.withType<MavenPublication>().forEach { publication ->
        val targetName = publication.name.substringAfterLast(":")
        val artifactId = if (targetName == "kotlinMultiplatform") {
            "\"<artifact-id>\""
        } else {
            "<artifact-id>-$targetName".lowercase()
        }
        publication.groupId = "com.timo.ai.a2a"
        publication.artifactId = artifactId
        publication.pom {
            name.set(rootProject.name)
            description.set(findProperty("publicationDescriptionLibrary") as String)
            url.set(findProperty("publicationUrl") as String)

            licenses {
                license {
                    name.set(findProperty("publicationLicenseName") as String)
                    url.set(findProperty("publicationLicenseUrl") as String)
                }
            }

            scm {
                url.set(findProperty("publicationScmUrl") as String)
                connection.set(findProperty("publicationScmConnection") as String)
                developerConnection.set(findProperty("publicationScmDeveloperConnection") as String)
            }

            developers {
                developer {
                    id.set(findProperty("publicationDeveloperId") as String)
                    name.set(findProperty("publicationDeveloperName") as String)
                }
            }
        }
    }
    */
}
