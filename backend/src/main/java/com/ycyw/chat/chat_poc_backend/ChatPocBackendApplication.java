package com.ycyw.chat.chat_poc_backend;

import com.ycyw.chat.chat_poc_backend.config.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppSecurityProperties.class)
public class ChatPocBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatPocBackendApplication.class, args);
	}

}
