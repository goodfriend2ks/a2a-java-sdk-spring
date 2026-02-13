package io.github.timo.a2a.spring.ai.agent.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HostAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HostAgentApplication.class, args);
    }
}
