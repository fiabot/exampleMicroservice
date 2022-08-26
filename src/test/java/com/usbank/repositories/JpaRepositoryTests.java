package com.usbank.repositories;



import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;
import com.usbank.exampleService.ExampleServiceApplication;
import com.usbank.models.Account;
import com.usbank.models.Transaction;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class)
public class JpaRepositoryTests {
	
	@Autowired 
	JpaAccountRepository accountRepo; 
	
	@Autowired 
	JpaTransactionRepository transRepo; 
	
	
	@Test
	 public void getAllAccount() {
		List<Account> accounts = accountRepo.findAll(); 
		assertNotEquals(accounts.size(), 0);
	}
	
	@Test
	 public void getAllAccountByNumber() {
		Account account = accountRepo.findByAccountNumber("1111").get(0);   
		assertEquals(account.getAccountNumber(), "1111"); 
		
	}
	
	@Test 
	public void saveAndDeleteAccount() {
		// save account 
		Account newAccount = new Account("INVALID", 42); 
		Account savedAccount = accountRepo.save(newAccount); 
		assertEquals(savedAccount.accountNumber, "INVALID"); 
		
		// delete account 
		accountRepo.deleteByAccountNumber("INVALID"); 
		List<Account> foundAccount = accountRepo.findByAccountNumber("INVALID"); 
		assertEquals(foundAccount.size(), 0);
	}
	
	@Test 
	public void saveUpdateAndDeleteAccount() {
		// save account 
		Account newAccount = new Account("INVALID", 100); 
		Account savedAccount = accountRepo.save(newAccount); 
		assertEquals(savedAccount.accountNumber, "INVALID"); 
		
		//update account 
		accountRepo.updateValue("INVALID", 42);
		List<Account> updatedAccount = accountRepo.findByAccountNumber("INVALID"); 
		assertEquals(42, updatedAccount.get(0).getValue());
		
		
		// delete account 
		accountRepo.deleteByAccountNumber("INVALID"); 
		List<Account> foundAccount = accountRepo.findByAccountNumber("INVALID"); 
		assertEquals(foundAccount.size(), 0); 
	}
	
	@Test 
	public void saveAndDeleteTransaction() {
		
		// set up fake accounts 
		Account fakeAccount1 = new Account("INVALID1", 1111); 
		Account fakeAccount2 = new Account("INVALID2", 2222); 
		accountRepo.save(fakeAccount1); 
		accountRepo.save(fakeAccount2); 
		
		// create transaction 
		Transaction newTransaction = new Transaction("INVALID1", "INVALID2", 20);
		Transaction createdTransaction = transRepo.save(newTransaction); 
		assertEquals(createdTransaction.getFromAccount(), "INVALID1"); 
		assertEquals(createdTransaction.getToAccount(), "INVALID2"); 
		int id = createdTransaction.getId(); 
		
		// pull transaction from repo 
		Transaction foundTransaction = transRepo.findById(id).get(); 
		assertEquals(foundTransaction.getFromAccount(), "INVALID1"); 
		assertEquals(foundTransaction.getToAccount(), "INVALID2"); 
		
		
		// delete transaction 
		transRepo.deleteById(id);
		Optional<Transaction> findAfterDelete = transRepo.findById(id); 
		assertFalse(findAfterDelete.isPresent());
		
		// delete accounts 
		accountRepo.deleteByAccountNumber("INVALID1");
		accountRepo.deleteByAccountNumber("INVALID2");
	}
	
	
	@Test(expected = DataIntegrityViolationException.class)
	public void createInvalidTransaction() {
		Transaction newTransaction = new Transaction("INVALID1", "INVALID2", 20);
		Transaction createdTransaction = transRepo.save(newTransaction); 
		 
	}
		

}
