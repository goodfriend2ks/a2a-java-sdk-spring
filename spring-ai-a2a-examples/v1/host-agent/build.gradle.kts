import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
}

group = "${property("libraryGroupId")}.example"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

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

/*
tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xjspecify-annotations=warn",
        )
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("started", "skipped", "passed", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    jvmArgs = listOf(
        "--add-opens",
        "java.base/java.time=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED",
        "-Dspring.test.context.cache.maxSize=2",
    )

    maxHeapSize = when {
        System.getenv("INTEGRATION_TEST_CI") == true.toString() -> "2048m"
        System.getenv("INTEGRATION_TEST_FULL") == true.toString() -> "8g"
        System.getenv("CI") == true.toString() -> "3g"
        else -> "16g"
    }
}
*/

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<org.gradle.api.tasks.bundling.Jar>("jar") {
    enabled = false
}
