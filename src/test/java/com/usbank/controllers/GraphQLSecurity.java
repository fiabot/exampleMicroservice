package com.usbank.controllers;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;


import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;
import com.usbank.exampleService.ExampleServiceApplication;
import com.usbank.models.Account;
import com.usbank.models.Transaction;
import static org.mockito.ArgumentMatchers.*;

/**
 *  Test security for GraphQL 
 *  method 
 * 
 * Repositories are mocked 
 * @author fmshyne
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class)

public class GraphQLSecurity {
	
	@Autowired 
	GraphQLController controller; 
	
	@MockBean 
	JpaAccountRepository accountRepo; 
	
	@MockBean 
	JpaTransactionRepository transRepo; 
	
	Account acc1; 
	Account acc2; 
	
	Transaction trans1; 
	Transaction trans2; 
	@Before
	public void accountSetup() {
		acc1 = new Account("1111", 1111); 
		acc2 = new Account("2222", 2222); 
		
		ArrayList<Account> accounts = new ArrayList<Account>(); 
		accounts.add(acc1); 
		accounts.add(acc2); 
		
		ArrayList<Account> account1 = new ArrayList<Account>(); 
		account1.add(acc1); 
		
		ArrayList<Account> account2 = new ArrayList<Account>(); 
		account2.add(acc2);

		
		Mockito.when(accountRepo.findAll()).thenReturn(accounts);
		Mockito.when(accountRepo.findByAccountNumber("1111")).thenReturn(account1); 
		Mockito.when(accountRepo.findByAccountNumber("2222")).thenReturn(account2); 
		Mockito.when(accountRepo.save(any(Account.class))).then(AdditionalAnswers.returnsFirstArg());
		Mockito.doNothing().when(accountRepo).deleteByAccountNumber(anyString()); 
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock a) throws Throwable { 
		    	if(a.getArgument(0).equals("1111")) {
		    		acc1.setValue(a.getArgument(1));
		    	}else if (a.getArgument(0).equals("2222")) {
		    		acc2.setValue(a.getArgument(1));
		    	}
		    	return null;
		    }
		  }).when(accountRepo).updateValue(anyString(), anyDouble()); 
	}
	
	@Before
	public void transactionSetup() {
		trans1 = new Transaction(1, "1111", "2222", 20); 
		trans2 = new Transaction(2, "2222", "1111", 42); 
		
		ArrayList<Transaction> allTrans = new ArrayList<Transaction>(); 
		allTrans.add(trans2); 
		allTrans.add(trans1); 
		
		ArrayList<Transaction> incomingTrans = new ArrayList<Transaction>(); 
		incomingTrans.add(trans2); 
		
		ArrayList<Transaction> outgoingTrans = new ArrayList<Transaction>(); 
		outgoingTrans.add(trans1); 
		
		Optional<Transaction> optTrans1 = Optional.of(trans1); 
		Optional<Transaction> optTrans2 = Optional.of(trans2); 
		
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock a) throws Throwable { 
		    	if(a.getArgument(0).equals(1)) {
		    		trans1.setStatus(a.getArgument(1));
		    	}else if (a.getArgument(0).equals(2)) {
		    		trans2.setStatus(a.getArgument(1));
		    	}
		    	return null;
		    }
		  }).when(transRepo).updateStatus(anyInt(), anyString()); 

		
		
		//InvocationOnMock i = new InvocationOnMock(); 
		Mockito.when(transRepo.findAll()).thenReturn(allTrans);  
		// return the input transaction instead of saving 
		Mockito.when(transRepo.save((Transaction)notNull())).then(AdditionalAnswers.returnsFirstArg()); 
		Mockito.when(transRepo.findById(1)).thenReturn(optTrans1); 
		Mockito.when(transRepo.findById(2)).thenReturn(optTrans2);
		Mockito.doNothing().when(transRepo).deleteById(anyInt()); 
		
		
		
	}
	
	
	@Test(expected = AccessDeniedException.class)
	@WithMockUser(username = "user1", authorities = {"user", "ACC_1111" })
	public void getAllAccountUser() {
		controller.getAllAccounts(); 
	}
	
	@Test
	@WithMockUser(username = "admin", authorities = { "admin", "user" })
	public void getAllAccountAdmin() {
		controller.getAllAccounts(); 
	}
	
	@Test
	@WithMockUser(username = "admin", authorities = {"admin", "user" })
	public void getAccountUserAdmin() {
		controller.getAccountByNumber("1111"); 
	}
	
	@Test
	@WithMockUser(username = "user1", authorities = {"user", "ACC_1111" })
	public void getAccountUserCorrect() {
		controller.getAccountByNumber("1111"); 
	}
	
	@Test(expected = AccessDeniedException.class)
	@WithMockUser(username = "user1", authorities = {"user", "ACC_2222" })
	public void getAccountUserWrong() {
		controller.getAccountByNumber("1111"); 
	}
	
	
	@Test(expected = AccessDeniedException.class)
	@WithMockUser(username = "user1", authorities = {"user", "ACC_3333" })
	public void getTransactionWrongUser() {
		controller.getTransactionById(1); 
	}
	
	@Test
	@WithMockUser(username = "user1", authorities = {"user", "ACC_1111" })
	public void getTransactionCorrectUser() {
		controller.getTransactionById(1); 
	}
	
	@Test
	@WithMockUser(username = "admin", authorities = { "admin", "user" })
	public void getTransactionAdmin() {
		controller.getTransactionById(1); 
	}
	
}
