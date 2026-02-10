plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
}

group = "${property("libraryGroupId")}.example"

dependencies {
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.grpc:spring-grpc-server-spring-boot-starter:${property("springGrpcVersion")}")

    implementation("org.springframework.ai:spring-ai-client-chat")
    implementation("org.springframework.ai:spring-ai-starter-model-azure-openai")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation(project(":spring-a2a-server"))
    implementation(project(":spring-a2a-server-autoconfigure"))

    implementation("io.github.a2asdk:a2a-java-sdk-server-common:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-grpc:${property("a2aSdkVersion")}")
    implementation("io.github.a2asdk:a2a-java-sdk-reference-jsonrpc:${property("a2aSdkVersion")}")
    // implementation("io.github.a2asdk:a2a-java-sdk-reference-grpc:${property("a2aSdkVersion")}")

    testImplementation(kotlin("test"))
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    jvmToolchain(21)
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
