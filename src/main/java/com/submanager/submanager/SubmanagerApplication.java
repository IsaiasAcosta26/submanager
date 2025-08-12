package com.submanager.submanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubmanagerApplication.class, args);
	}

}
