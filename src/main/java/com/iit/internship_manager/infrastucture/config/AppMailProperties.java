package com.iit.internship_manager.infrastucture.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {
    private String from = "pfaservice2012@gmail.com";
}
