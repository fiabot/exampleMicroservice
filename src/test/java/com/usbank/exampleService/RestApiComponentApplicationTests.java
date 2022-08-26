package com.usbank.exampleService;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;
import com.usbank.models.Account;
import com.usbank.models.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.List;
import java.util.Map;

/**
 * Component testing for REST API 
 * 
 * Role play an end user attempting to 
 * use the REST API 
 * @author fmshyne
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class)
@AutoConfigureMockMvc
class RestApiComponentApplicationTests {
	
	@Autowired
    private MockMvc mvc;
	
	// add repositories to make clean up easier 
	@Autowired 
	JpaAccountRepository accountRepo; 
	
	@Autowired 
	JpaTransactionRepository transRepo; 

	@Test
	void restUser1Test() throws Exception {
		Gson gson = new Gson(); 
		// get authorized 
		
		MvcResult tokenResult = mvc.perform(post("/authenticate/")
				.param("username", "user1").param("password", "IAmUser1")).andReturn();
		
		assertEquals(null, tokenResult.getResolvedException()); 
		
		String response = tokenResult.getResponse().getContentAsString(); 
		Map<String, Object> responseMap = gson.fromJson(response, new TypeToken<Map<String, Object>>() {
		}.getType());
		
		String token = (String) responseMap.get("token"); 
		
		// get account details 
		MvcResult accountResult = mvc.perform(get("/user/getAccountByNumber/")
				.param("accountNumber", "1111").header("Authorization", "Bearer " + token))
				.andReturn(); 
		
		String accountResponse = accountResult.getResponse().getContentAsString(); 
		List<Account> account  = gson.fromJson(accountResponse, new TypeToken<List<Account>>() {
		}.getType());
		
		assertEquals(account.get(0).getAccountNumber(), "1111"); 
		
		
		// attempt to get another account details 
		mvc.perform(get("/user/getAccountByNumber/")
				.param("accountNumber", "2222").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden()); 
		
		
		// attempt to send an admin request 
		mvc.perform(get("/admin/getAllAccounts/")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden()); 
		
		
		// create a transaction 
		MvcResult transResult = mvc.perform(post("/user/createTransaction/")
				.param("fromAccount", "1111").param("toAccount", "2222").param("value", "100")
				.header("Authorization", "Bearer " + token)).andReturn();
		
		String transResponse = transResult.getResponse().getContentAsString(); 
		Transaction trans = gson.fromJson(transResponse, Transaction.class);
		int transId =  trans.getId();
		try {
			 
			assertEquals(trans.getFromAccount(), "1111"); 
			assertEquals(trans.getToAccount(), "2222"); 
			assertEquals(trans.getStatus(), Transaction.NOTPROCCESSED); 
			
		}  catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		// get the transaction 
		try{
			MvcResult transResult2 = mvc.perform(get("/user/getTransactionById/")
					.param("id", String.valueOf(transId))
					.header("Authorization", "Bearer " + token)).andReturn();
			
			String transResponse2 = transResult2.getResponse().getContentAsString(); 
			List<Transaction> tran2 = gson.fromJson(transResponse2, new TypeToken<List<Transaction>>() {
			}.getType());  
			assertEquals(transId, tran2.get(0).getId()); 
			
			
		}catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		transRepo.deleteById(transId);
		//attempt to create an transaction from another account 
		mvc.perform(post("/user/createTransaction/")
				.param("fromAccount", "3333").param("toAccount", "2222").param("value", "100")
				.header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
	
	}
	
	@Test
	void restAdminTest() throws Exception {
		Gson gson = new Gson(); 
		// get authorized 
		
		MvcResult tokenResult = mvc.perform(post("/authenticate/")
				.param("username", "admin").param("password", "IAmAdmin")).andReturn();
		
		assertEquals(null, tokenResult.getResolvedException()); 
		
		String response = tokenResult.getResponse().getContentAsString(); 
		Map<String, Object> responseMap = gson.fromJson(response, new TypeToken<Map<String, Object>>() {
		}.getType());
		
		String token = (String) responseMap.get("token"); 
		
		// get account details 
		MvcResult accountResult = mvc.perform(get("/user/getAccountByNumber/")
				.param("accountNumber", "1111").header("Authorization", "Bearer " + token))
				.andReturn(); 
		
		String accountResponse = accountResult.getResponse().getContentAsString(); 
		List<Account> account  = gson.fromJson(accountResponse, new TypeToken<List<Account>>() {
		}.getType());
		
		assertEquals(account.get(0).getAccountNumber(), "1111"); 
		
		
		
		// attempt to send an admin request 
		MvcResult allAccResult = mvc.perform(get("/admin/getAllAccounts/")
				.header("Authorization", "Bearer " + token))
				.andReturn();
		String allAccResponse = allAccResult.getResponse().getContentAsString(); 
		List<Account> accounts = gson.fromJson(allAccResponse, new TypeToken<List<Account>>() {
		}.getType());
		
		assertNotEquals(0, accounts.size()); 
		
		
		
		// create a transaction 
		MvcResult transResult = mvc.perform(post("/user/createTransaction/")
				.param("fromAccount", "1111").param("toAccount", "2222").param("value", "100")
				.header("Authorization", "Bearer " + token)).andReturn();
		
		String transResponse = transResult.getResponse().getContentAsString(); 
		Transaction trans = gson.fromJson(transResponse, Transaction.class);
		int transId =  trans.getId();
		try {
			 
			assertEquals(trans.getFromAccount(), "1111"); 
			assertEquals(trans.getToAccount(), "2222"); 
			assertEquals(trans.getStatus(), Transaction.NOTPROCCESSED); 
			
		}  catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		// get the transaction 
		try{
			MvcResult transResult2 = mvc.perform(get("/user/getTransactionById/")
					.param("id", String.valueOf(transId))
					.header("Authorization", "Bearer " + token)).andReturn();
			
			String transResponse2 = transResult2.getResponse().getContentAsString(); 
			List<Transaction> tran2 = gson.fromJson(transResponse2, new TypeToken<List<Transaction>>() {
			}.getType());  
			assertEquals(transId, tran2.get(0).getId()); 
			
			
		}catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		transRepo.deleteById(transId);
		
		
		// create an account 
		MvcResult newAccountResult = mvc.perform(post("/admin/createAccount/")
				.param("accountNumber", "666").param("value", "666")
				.header("Authorization", "Bearer " + token))
				.andReturn(); 
		System.out.println("Hello World");
		try {
			String newAccountResponse = newAccountResult.getResponse().getContentAsString(); 
			System.out.println(newAccountResponse); 
			Account newAccount = gson.fromJson(newAccountResponse, Account.class); 
			
			assertEquals(newAccount.getAccountNumber(), "666"); 
			assertEquals(newAccount.getValue(), 666, 0.5); 
			
		}catch (Exception e) {
			accountRepo.deleteByAccountNumber("666");
			throw(e); 
			
		} catch (Error e) {
			accountRepo.deleteByAccountNumber("666");
			throw(e); 
		}
	
	}
	
	

}
