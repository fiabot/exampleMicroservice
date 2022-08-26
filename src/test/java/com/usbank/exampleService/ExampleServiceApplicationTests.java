package com.usbank.exampleService;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class ExampleServiceApplicationTests {
	
	@Autowired
    private MockMvc mvc;
	
	// add repositories to make clean up easier 
	@Autowired 
	JpaAccountRepository accountRepo; 
	
	@Autowired 
	JpaTransactionRepository transRepo; 
	
	@Test 
	public void notNullTest() {
		assertNotNull(mvc); 
		assertNotNull(accountRepo); 
		assertNotNull(transRepo); 
	}
	

}
