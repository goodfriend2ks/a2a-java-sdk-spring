plugins {
    id("java")
}

group = "com.timo.ai.a2a"
version = "1.0-SNAPSHOT"
description = "Library to support for Spring AI A2A Server"

repositories {
    mavenCentral()
}

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
