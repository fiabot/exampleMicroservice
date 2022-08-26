package com.usbank.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import com.usbank.Repositories.JdbcAccountManagementRepository;
import com.usbank.Repositories.JdbcTransactionManagementRepository;
import com.usbank.exampleService.ExampleServiceApplication;
import com.usbank.models.Account;
import com.usbank.models.Transaction;

/**
 * Unit tests for Rest account and transaction controllers 
 * 
 * Repositories are mocked 
 * @author fmshyne
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class)
@WithMockUser(username = "admin", authorities = { "admin", "user" })
public class RestControllers {
	
	@Autowired 
	RestAccountMangementController accountController; 
	
	@Autowired 
	RestTransactionMangementController transController; 
	
	@MockBean 
	JdbcAccountManagementRepository accountrepo; 
	
	@MockBean 
	JdbcTransactionManagementRepository transrepo;
	
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

		
		Mockito.when(accountrepo.getAllAccounts()).thenReturn(accounts);
		Mockito.when(accountrepo.getAccount("1111")).thenReturn(account1); 
		Mockito.when(accountrepo.postAccount("1111", 1111)).thenReturn(acc1); 
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
		
		Mockito.when(transrepo.getAllTransactions()).thenReturn(allTrans); 
		Mockito.when(transrepo.getIncomingTransactions("1111")).thenReturn(incomingTrans); 
		Mockito.when(transrepo.getOutgoingTransactions("1111")).thenReturn(outgoingTrans); 
		Mockito.when(transrepo.postTransaction("1111", "2222", 20)).thenReturn(trans1); 
		Mockito.when(transrepo.getTransactionFromId(1)).thenReturn(outgoingTrans); 
		
	}
	
	
	@Test
	public void getAllAccounts() {
		ResponseEntity<?> response = accountController.getAllAccounts();
		List<Account> accounts = (List<Account>) response.getBody(); 
		
		assertTrue(accounts.size() == 2); 
		assertTrue(response.getStatusCode().equals(HttpStatus.OK)); 
		
		for(Account account : accounts) {
			if(account.getAccountNumber().equals("1111")){
				assertTrue(account.getValue() == 1111); 
			}else {
				assertTrue(account.getValue() == 2222); 
			}
		}
	}
	
	@Test
	public void getAccount() {
		ResponseEntity<?> response = accountController.getAccountByNumber("1111");
		List<Account> accounts = (List<Account>) response.getBody(); 
		
		assertTrue(accounts.size() == 1); 
		assertTrue(response.getStatusCode().equals(HttpStatus.OK)); 
		
		Account account = accounts.get(0); 
		assertTrue(account.getValue() == 1111); 
	}
	
	@Test 
	public void createAccount() {
		ResponseEntity<?> response = accountController.createAccount("1111", 1111); 
		Account account = (Account) response.getBody(); 
		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(account.equals(acc1)); 
	}
	
	
	@Test 
	public void getAllTransactions() {
		ResponseEntity<?> response = transController.getAllTransactions(); 
		List<Transaction> transactions = (List<Transaction>) response.getBody(); 
		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(transactions.size() == 2); 
		
		for(Transaction tran : transactions) {
			if(tran.getId() == 1) {
				assertTrue(tran.equals(trans1)); 
			}else {
				assertTrue(tran.equals(trans2)); 
			}
		}
	}
	
	
	@Test
	public void getTransactionById(){
		ResponseEntity<?> response = transController.getTransactionById(1); 
		List<Transaction> transactions = (List<Transaction>) response.getBody(); 
		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(transactions.size() == 1); 
		
		assertTrue(transactions.get(0).equals(trans1)); 
		
	}
	
	
	@Test
	public void getOutgoingTransactions(){
		ResponseEntity<?> response = transController.getOutgoingTransactions("1111"); 
		List<Transaction> transactions = (List<Transaction>) response.getBody(); 
		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(transactions.size() == 1); 
		
		assertTrue(transactions.get(0).equals(trans1)); 
		
	}
	
	@Test
	public void getIncomingTransactions(){
		ResponseEntity<?> response = transController.getIncomingTransactions("1111"); 
		List<Transaction> transactions = (List<Transaction>) response.getBody(); 
		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(transactions.size() == 1); 
		
		assertTrue(transactions.get(0).equals(trans2)); 
		
	}
	
	@Test
	public void createTransaction(){
		ResponseEntity<?> response = transController.createTransaction("1111", "2222", 20); 
		Transaction transaction = (Transaction) response.getBody(); 
		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		
		assertTrue(transaction.equals(trans1)); 
		
	}

}
