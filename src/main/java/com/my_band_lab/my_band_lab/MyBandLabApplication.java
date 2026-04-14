package com.my_band_lab.my_band_lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.my_band_lab.my_band_lab", "com.mybandlab"})
public class MyBandLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyBandLabApplication.class, args);
	}

}
