package com.bdool.memberhubservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MemberHubServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberHubServiceApplication.class, args);
	}

}
