package com.usbank.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.*;

import com.usbank.Repositories.JdbcAccountManagementRepository;
import com.usbank.Repositories.JdbcTransactionManagementRepository;
import com.usbank.exampleService.ExampleServiceApplication;
import com.usbank.models.Account;
import com.usbank.models.Transaction;
import com.usbank.security.JwtUserDetailsService;
import com.usbank.security.JwtUtil;


/**
 * Test security for REST API 
 * 
 * Repositories are mocked 
 * @author fmshyne
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class)
@AutoConfigureMockMvc
public class RestSecurityTest {
	
	@Autowired 
	JwtUtil tokenUtil; 
	
	@Autowired 
	JwtUserDetailsService userService; 
	
	@Autowired 
	RestAccountMangementController accountController; 
	
	@Autowired 
	RestTransactionMangementController transController; 
	
	@MockBean 
	JdbcAccountManagementRepository accountrepo; 
	
	@MockBean 
	JdbcTransactionManagementRepository transrepo;
	
	@Autowired
    private MockMvc mvc;
	
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
		Transaction trans3 = new Transaction(3, "3333", "1111", 52); 
		
		ArrayList<Transaction> allTrans = new ArrayList<Transaction>(); 
		allTrans.add(trans2); 
		allTrans.add(trans1); 
		allTrans.add(trans3); 
		
		ArrayList<Transaction> incomingTrans = new ArrayList<Transaction>(); 
		incomingTrans.add(trans2); 
		
		ArrayList<Transaction> outgoingTrans = new ArrayList<Transaction>(); 
		outgoingTrans.add(trans1); 
		
		
		Mockito.when(transrepo.getAllTransactions()).thenReturn(allTrans); 
		Mockito.when(transrepo.getIncomingTransactions("1111")).thenReturn(incomingTrans); 
		Mockito.when(transrepo.getOutgoingTransactions("1111")).thenReturn(outgoingTrans); 
		Mockito.when(transrepo.postTransaction("1111", "2222", 20)).thenReturn(trans1); 
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock a) throws Throwable { 
		    	ArrayList<Transaction> transaction = new ArrayList<Transaction>(); 
		    	if(a.getArgument(0).equals(1)) {
		    		transaction.add(trans1); 
		    	}else if(a.getArgument(0).equals(2)) {
		    		transaction.add(trans2); 
		    	}else if(a.getArgument(0).equals(3)) {
		    		transaction.add(trans3); 
		    	}
		    	return transaction;
		    }
		  }).when(transrepo).getTransactionFromId(anyInt()); 
		
	}
	
	private String getAdminToken() {
		UserDetails admin =  userService.loadUserByUsername("admin"); 
		return "Bearer "  + tokenUtil.generateToken(admin); 
	}
	
	private String getUser1Token() {
		UserDetails user1 =  userService.loadUserByUsername("user1"); 
		return "Bearer "  +  tokenUtil.generateToken(user1); 
	}
	
	private String getUser2Token() {
		UserDetails user2 =  userService.loadUserByUsername("user2"); 
		return  "Bearer "  +  tokenUtil.generateToken(user2); 
	}
	
	@Test
	public void getAllAccountAdmin() {
		try {
			mvc.perform(get("/admin/getAllAccounts/").header("Authorization", getAdminToken())).andExpect(status().isOk());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
	
	@Test
	public void getAllAccountUser() {
		try {
			mvc.perform(get("/admin/getAllAccounts/").header("Authorization", getUser1Token())).andExpect(status().isForbidden());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
	
	
	@Test
	public void getAccountCorrectUser() {
		try {
			mvc.perform(get("/user/getAccountByNumber/").header("Authorization", getUser1Token()).param("accountNumber", "1111")).andExpect(status().isOk());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
	
	@Test
	public void getAccountIncorrectUser() {
		try {
			mvc.perform(get("/user/getAccountByNumber/").param("accountNumber", "1111").header("Authorization", getUser2Token())).andExpect(status().isForbidden());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
	
	@Test
	public void getAccountAdmin() {
		try {
			mvc.perform(get("/user/getAccountByNumber/").header("Authorization", getAdminToken()).param("accountNumber", "1111")).andExpect(status().isOk());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
	
	@Test
	public void getTransactionAdmin() {
		try {
			mvc.perform(get("/user/getTransactionById/").header("Authorization", getAdminToken()).param("id", "3")).andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace(); 
			assertTrue(false); 
		} 
	}
	
	@Test
	public void getTransactionCorrectUser() {
		try {
			mvc.perform(get("/user/getTransactionById/").header("Authorization", getUser1Token()).param("id", "3")).andExpect(status().isOk());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
	
	@Test
	public void getTransactionIncorrectUser() {
		try {
			mvc.perform(get("/user/getTransactionById/").header("Authorization", getUser2Token()).param("id", "3")).andExpect(status().isForbidden());
		} catch (Exception e) {
			assertTrue(false); 
		} 
	}
}
