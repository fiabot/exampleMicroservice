package com.usbank.exampleService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * scan files and start 
 * server at localhost:8080 
 * 
 * two main ways to access api: 
 * 		graphQL version: localhost:8080\graphiql\
 * 		REST/Swagger version: localhost:8080\swagger-ui\ 
 * 
 * GraphQl version uses jpa based repositories 
 * Rest uses jdbcTemplate based repositories 
 * @author fmshyne
 *
 */
@SpringBootApplication
@EnableSwagger2
@EnableJpaRepositories(basePackages = "com.usbank.Repositories")
@ComponentScan("com.usbank.*")
@EntityScan("com.usbank.*") 
public class ExampleServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleServiceApplication.class, args);
	}
	
	

}