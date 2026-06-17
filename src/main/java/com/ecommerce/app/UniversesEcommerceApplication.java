package com.ecommerce.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UniversesEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniversesEcommerceApplication.class, args);
	}

}
