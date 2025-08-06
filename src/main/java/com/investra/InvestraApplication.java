package com.investra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Zamanlanmış görevleri etkinleştir
public class InvestraApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestraApplication.class, args);
	}
}
