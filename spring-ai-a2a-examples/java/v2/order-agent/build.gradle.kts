plugins {
    java
    id("org.springframework.boot")
}

group = "${property("libraryGroupId")}.example"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.grpc:spring-grpc-server-spring-boot-starter:${property("springGrpcVersion")}")

    implementation("org.springframework.ai:spring-ai-client-chat")
    implementation("org.springframework.ai:spring-ai-starter-model-azure-openai")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(project(":spring-a2a-server"))
    implementation(project(":spring-ai-a2a-tool"))
    implementation(project(":spring-a2a-server-autoconfigure"))
    implementation(project(":spring-ai-a2a-tool-autoconfigure"))

    implementation("io.github.a2asdk:a2a-java-sdk-server-common:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-grpc:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-reference-jsonrpc:${property("a2aSdkVersion")}") {
        exclude("org.jboss.slf4j", "slf4j-jboss-logmanager")
    }
    // implementation("io.github.a2asdk:a2a-java-sdk-reference-grpc:${property("a2aSdkVersion")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<org.gradle.api.tasks.bundling.Jar>("jar") {
    enabled = false
}
