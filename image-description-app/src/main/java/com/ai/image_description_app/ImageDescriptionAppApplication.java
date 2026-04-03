package com.ai.image_description_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
public class ImageDescriptionAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageDescriptionAppApplication.class, args);
	}

}