plugins {
    id("java")

    id("com.vanniktech.maven.publish")
}

description = "Server implementation for Spring A2A protocol support"

dependencies {
    implementation("io.projectreactor:reactor-core")

    implementation("org.springframework:spring-web")
    implementation("org.springframework.security:spring-security-core")

    implementation("io.github.a2asdk:a2a-java-sdk-server-common:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-grpc:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-jsonrpc:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-rest:${property("a2aSdkVersion")}")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
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
