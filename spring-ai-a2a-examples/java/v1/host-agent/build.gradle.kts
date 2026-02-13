plugins {
    java
    id("org.springframework.boot")
}

group = "${property("libraryGroupId")}.example"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.ai:spring-ai-client-chat")
    implementation("org.springframework.ai:spring-ai-starter-model-azure-openai")

    implementation("io.github.a2asdk:a2a-java-sdk-client:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-client-transport-grpc:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-client-transport-jsonrpc:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-client-transport-rest:${property("a2aSdkVersion")}")

    // [Note: important for GRPC protocol]
    implementation("io.grpc:grpc-netty:1.78.0")
    // implementation("io.grpc:grpc-netty-shaded:1.78.0")
    // implementation("io.grpc:grpc-okhttp:1.78.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<org.gradle.api.tasks.bundling.Jar>("jar") {
    enabled = false
}
