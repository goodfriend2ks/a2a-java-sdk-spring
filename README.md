# A2A Java SDK for Spring Framework

This is the integration of the [A2A Java SDK](https://github.com/a2aproject/a2a-java) for Spring framework web servers.
Including server, Spring Boot auto-configuration, Spring AI tools, and Spring AI examples.

This implementation is aligned with **A2A Protocol Specification 1.0.0**.

For more information about the A2A protocol, see [here](https://github.com/a2aproject/A2A).

## Getting Started

To use the A2A Java SDK in your Spring Boot application, you will need to package it as a `.jar` file by using supported `spring-boot-autoconfigure` projects. 

### Packaging your Spring Boot application

The key to enabling A2A in your Java application is to correctly package it. Here are the general steps you need to follow:

1. **Manage Dependencies:**
    * **Using the Spring Boot autoconfigure:**
    ```bash
        {
            dependencies {
                implementation("com.timo.ai.a2a:spring-a2a-server:${property("a2aSdkSpringVersion")}")
                implementation("com.timo.ai.a2a:spring-a2a-server-autoconfigure:${property("a2aSdkSpringVersion")}")
            }
        }
    ```

    * **(Optional) Using the Spring Boot autoconfigure for `Spring AI` supported Agent skill:**
    ```bash
        {
            dependencies {
                implementation("com.timo.ai.a2a:spring-ai-a2a-tool:${property("a2aSdkSpringVersion")}")
                implementation("com.timo.ai.a2a:spring-ai-a2a-server-autoconfigure:${property("a2aSdkSpringVersion")}")
            }
        }
    ```
   
    * **(Optional) Using the GRPC Spring Boot autoconfigure to support GRPC protocol:**
    ``` bash
        {
            dependencies {
                implementation("org.springframework.grpc:spring-grpc-server-spring-boot-starter:${property("springGrpcVersion")}")
            }
        }
    ```

2.  **Provide implementations for `AgentExecutor` and `AgentCard`:** The A2A SDK requires you to provide your own implementations of the `AgentExecutor` and `AgentCard` interfaces.

    2.1.  **Implementation for `AgentCard`:**
       * **Option 1:** Implement your own skills for your agent. You can find more information about them in the [A2A Java SDK documentation](https://github.com/a2aproject/a2a-java?tab=readme-ov-file#2-add-a-class-that-creates-an-a2a-agent-card). See examples in `spring-ai-a2a-examples/v1`
       * **Option 2:** Using Spring Boot autoconfigure for `Spring AI` supported Agent Skills: See examples in `spring-ai-a2a-examples/v2`.
    2.2.  **Implementations for `AgentExecutor`:**
       * **Option 1:** Implement your own `AgentExecutor`. You can find more information about them in the [A2A Java SDK documentation](https://github.com/a2aproject/a2a-java?tab=readme-ov-file#3-add-a-class-that-creates-an-a2a-agent-executor).
       * **Option 2:** Use default `AgentExecutor` implementation and implement `AgentExecutorHandler` to provide your agent business logic.

3. **Manage Configurations:**
    ```yaml
    spring:
      ai:
        a2a:
          server:
            enabled: true
            host: "*"
            ssl: false
            grpc:
              enabled: false
            jsonrpc:
              enabled: false
            rest:
              enabled: false
    ```

## Examples
There are two version of Spring Boot application examples with Spring AI (in `spring-ai-a2a-examples` folder):
1. Version 1 (`v1`): Provide example that implement own skills for AI agent
2. Version 1 (`v2`): Provide example that using auto-generate agent skills for AI agent via `AgentSkill` and `SkillAction` annotations.

## Release process
   1. Create release branch
   ```bash
        git checkout main
        git pull origin main
        git checkout -b releases/vX.X.X
   ```

   2. Update CHANGELOG.md
   ```text
    # Update version in relevant files
    # Update CHANGELOG.md
   ```

   3. Commit and push release branch
   ```bash
    commit -m "Update CHANGELOG.md for release vX.X.X"
    git push origin releases/vX.X.X
   ```

   4. Create and push release tag
   ```bash
    git tag vX.X.X
    git push origin vX.X.X
   ```

