plugins {
    id("java")

    id("com.vanniktech.maven.publish")
}

description = "Library to support for Spring AI A2A Server"

dependencies {
    implementation("org.springframework:spring-web")
    implementation("org.springframework.security:spring-security-core")

    implementation("org.springframework.ai:spring-ai-model")

    implementation(project(":spring-a2a-server"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc") // Sets name to <projectName>-<version>-javadoc.jar
    from(tasks.javadoc) // Takes output from the 'javadoc' task
}

signing {
    useGpgCmd()
    // sign(publishing.publications["mavenJava"])
}

publishing {
    repositories {
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
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), project.name, version.toString())

    pom {
        name = project.name
        description = project.description
        inceptionYear = "2026"
        url = "https://github.com/goodfriend2ks/a2a-java-sdk-spring"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "goodfriend2ks"
                name = "Timo"
                url = "https://github.com/goodfriend2ks"
            }
        }
        scm {
            url = "https://github.com/goodfriend2ks/a2a-java-sdk-spring/"
            connection = "scm:git:git://github.com:goodfriend2ks/a2a-java-sdk-spring.git"
            developerConnection = "scm:git:ssh://git@github.com:goodfriend2ks/a2a-java-sdk-spring.git"
        }
    }
}
