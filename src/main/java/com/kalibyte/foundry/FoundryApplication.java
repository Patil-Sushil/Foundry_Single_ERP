package com.kalibyte.foundry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.kalibyte.foundry.config.AppConfig;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class FoundryApplication {
	public static void main(String[] args) {
		SpringApplication.run(FoundryApplication.class, args);
	}
}
