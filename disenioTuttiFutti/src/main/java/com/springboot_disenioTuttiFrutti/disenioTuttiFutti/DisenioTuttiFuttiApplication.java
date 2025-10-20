package com.springboot_disenioTuttiFrutti.disenioTuttiFutti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = "com.springboot_disenioTuttiFrutti",
    exclude = {DataSourceAutoConfiguration.class}
)
public class DisenioTuttiFuttiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DisenioTuttiFuttiApplication.class, args);
	}

}
