rootProject.name = "a2a-java-sdk-spring"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

include("spring-a2a-server")
include("spring-a2a-server-autoconfigure")

include("spring-ai-a2a-tool")
include("spring-ai-a2a-tool-autoconfigure")

include("spring-ai-a2a-examples:v1:host-agent")
include("spring-ai-a2a-examples:v1:order-agent")
include("spring-ai-a2a-examples:v1:seller-agent")
include("spring-ai-a2a-examples:v2:order-agent")
