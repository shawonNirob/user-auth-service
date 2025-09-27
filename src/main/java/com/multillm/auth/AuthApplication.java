package com.multillm.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class AuthApplication {

	public static void main(String[] args) {
		log.info("Starting AuthApplication");
		SpringApplication.run(AuthApplication.class, args);
	}

}
