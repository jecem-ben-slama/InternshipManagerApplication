package com.iit.internship_manager.infrastucture.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private boolean enabled = false;
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4.1-mini";
    private String chatbotSystemPrompt;
}
