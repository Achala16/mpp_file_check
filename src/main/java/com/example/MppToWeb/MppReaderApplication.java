package com.example.MppToWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.MppToWeb.model")
@EnableJpaRepositories("com.example.MppToWeb.repository")
public class MppReaderApplication {
	public static void main(String[] args) {
		SpringApplication.run(MppReaderApplication.class, args);
	}
}