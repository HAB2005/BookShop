package com.example.system_backend;

import com.example.system_backend.common.config.FileUploadProperties;
import com.example.system_backend.common.config.GoogleProperties;
import com.example.system_backend.common.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ FileUploadProperties.class, JwtProperties.class, GoogleProperties.class })
public class SystemBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemBackendApplication.class, args);
	}

}
