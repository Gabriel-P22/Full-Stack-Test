package com.appointment.frameworks.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.appointment")
@EntityScan("com.appointment")
@EnableJpaRepositories("com.appointment")
@ConfigurationPropertiesScan("com.appointment")
public class AppointmentWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentWorkerApplication.class, args);
	}

}
