package com.landvn.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LandvnBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LandvnBackendApplication.class, args);
	}

}
