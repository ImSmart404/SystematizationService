package com.mikhaylov.diplom;

import org.springframework.boot.SpringApplication;

public class TestTextSystematizatorApplication {

	public static void main(String[] args) {
		SpringApplication.from(TextSystematizatorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
