package com.iit.internship_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class InternshipManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(InternshipManagerApplication.class, args);
	}
}
