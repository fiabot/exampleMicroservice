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

import com.usbank.Repositories.JdbcAccountManagementRepository;
import com.usbank.Repositories.JdbcTransactionManagementRepository;
import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;
import com.usbank.exampleService.ExampleServiceApplication;
import com.usbank.models.Account;
import com.usbank.models.Transaction;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class)
public class JdbcRepositoryTests {
	
	@Autowired 
	JdbcAccountManagementRepository accountRepo; 
	
	@Autowired 
	JdbcTransactionManagementRepository transRepo; 
	
	
	@Test
	 public void getAllAccount() {
		List<Account> accounts = accountRepo.getAllAccounts(); 
		assertNotEquals(accounts.size(), 0);
	}
	
	@Test
	 public void getAllAccountByNumber() {
		Account account = accountRepo.getAccount("1111").get(0);   
		assertEquals(account.getAccountNumber(), "1111"); 
		
	}
	
	@Test 
	public void saveAndDeleteAccount() {
		// save account 
		
		Account savedAccount = accountRepo.postAccount("INVALID", 42); 
		assertEquals(savedAccount.accountNumber, "INVALID"); 
		
		// delete account 
		accountRepo.deleteAccountByNumber("INVALID"); 
		List<Account> foundAccount = accountRepo.getAccount("INVALID"); 
		assertEquals(foundAccount.size(), 0);
	}
	
	
	
	@Test 
	public void saveAndDeleteTransaction() {
		
		Account savedAccount1 = accountRepo.postAccount("INVALID1", 42);
		Account savedAccount2 = accountRepo.postAccount("INVALID2", 42);
		
		// create transaction 
		Transaction newTransaction = new Transaction("INVALID1", "INVALID2", 20);
		Transaction createdTransaction = transRepo.postTransaction("INVALID1", "INVALID2", 20); 
		assertEquals(createdTransaction.getFromAccount(), "INVALID1"); 
		assertEquals(createdTransaction.getToAccount(), "INVALID2"); 
		int id = createdTransaction.getId(); 
		
		// pull transaction from repo 
		Transaction foundTransaction = transRepo.getTransactionFromId(id).get(0); 
		assertEquals(foundTransaction.getFromAccount(), "INVALID1"); 
		assertEquals(foundTransaction.getToAccount(), "INVALID2"); 
		
		
		// delete transaction 
		transRepo.deleteTransactionById(id);
		List<Transaction> findAfterDelete = transRepo.getTransactionFromId(id); 
		assertEquals(0, findAfterDelete.size()); 
		
		// delete accounts 
		accountRepo.deleteAccountByNumber("INVALID1");
		accountRepo.deleteAccountByNumber("INVALID2");
	}
	
	
	@Test(expected = DataIntegrityViolationException.class)
	public void createInvalidTransaction() {
		Transaction newTransaction = new Transaction("INVALID1", "INVALID2", 20);
		Transaction createdTransaction = transRepo.postTransaction("INVALID1", "INVALID2", 20); 
		 
	}
		

}
