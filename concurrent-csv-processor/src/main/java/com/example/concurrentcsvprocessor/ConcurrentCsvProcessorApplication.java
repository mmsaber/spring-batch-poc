package com.example.concurrentcsvprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConcurrentCsvProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrentCsvProcessorApplication.class, args);
	}

}
