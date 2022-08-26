package com.usbank.exampleService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.google.gson.Gson;
import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;
import com.usbank.models.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment; 


/**
 * Component testing for GraphQL 
 * 
 * Role play an end user using the 
 * GraphQL API 
 * @author fmshyne
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleServiceApplication.class, webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GraphqlApiComponentApplicationTests {
	
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
		String authQuery = "query GetUser($user : String ,  $pass : String){\n" + 
				"  \n" + 
				"	getAuthToken(username: $user, password: $pass)\n" + 
				"}"; 
	    JSONObject variables = new JSONObject();
	    variables.put("user", "user1"); 
	    variables.put("pass", "IAmUser1");
	    
		MvcResult authResult = mvc.perform(post("/graphql")
		        .content(generateRequest(authQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String authResponse = mvc.perform(asyncDispatch(authResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		
		JSONObject data = parseResponse(authResponse); 
		String token = data.getString("getAuthToken"); 
		
		// get account info 
		String getAccountQuery = "query \n" + 
				"  GetAccount($accountNumber: String ){\n" + 
				"    getAccountByNumber(accountNumber: $accountNumber){\n" + 
				"    accountNumber, \n" + 
				"    value \n" + 
				"  	}\n" + 
				"  }"; 
	    variables = new JSONObject();
	    variables.put("accountNumber", "1111"); 

	    MvcResult accResult = mvc.perform(post("/graphql")
	    		.header("Authorization", "Bearer " + token)
		        .content(generateRequest(getAccountQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String accResponse = mvc.perform(asyncDispatch(accResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		JSONObject accData = parseResponse(accResponse).getJSONObject("getAccountByNumber"); 
		
		assertEquals("1111", accData.getString("accountNumber")); 
		
		
		// attempt to get another account details 
		 variables = new JSONObject();
		 variables.put("accountNumber", "2222"); 

			
		
		 accResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(getAccountQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		accResponse = mvc.perform(asyncDispatch(accResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		assertTrue(isForbidden(accResponse)); 
		
		
		// attempt to send an admin request 
		String adminQuery = "query {\n" + 
				"  getAllAccounts{\n" + 
				"    accountNumber, \n" + 
				"    value\n" + 
				"  }\n" + 
				"}"; 
		
		variables = new JSONObject(); 
		MvcResult adminResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(adminQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String adminResponse = mvc.perform(asyncDispatch(adminResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
	
		assertTrue(isForbidden( adminResponse )); 
		
		
		// create a transaction 
		String transQuery = "query GetTrans($fromAccount: String!, $toAccount : String!, $value: Float){\n" + 
				"  createTransaction(fromAccount: $fromAccount, toAccount:$toAccount, value: $value){\n" + 
				"    id, \n" + 
				"    status, \n" + 
				"    fromAccount, \n" + 
				"    toAccount \n" + 
				"  }\n" + 
				"}"; 
		
		variables = new JSONObject(); 
		variables.put("fromAccount", "1111"); 
		variables.put("toAccount", "2222"); 
		variables.put("value", 100);
		
		MvcResult transResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(transQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String transResponse = mvc.perform(asyncDispatch(transResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		assertFalse(isForbidden(transResponse)); 
		JSONObject transJson = parseResponse(transResponse); 
		assertNotNull(transJson.getString("createTransaction")); 
		Transaction trans = gson.fromJson(transJson.getString("createTransaction"), Transaction.class);
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
		
		
		
		//get the transaction 
		try{
			
			String getQuery = "query GetTrans($id : Int!){\n" + 
					"  getTransactionById(id: $id){\n" + 
					"    fromAccount, \n" + 
					"    toAccount, \n" + 
					"    id, \n" + 
					"    value\n" + 
					"  }\n" + 
					"}"; 
			variables = new JSONObject(); 
			variables.put("id", transId); 
			
			MvcResult transResult2 = mvc.perform(post("/graphql")
					.header("Authorization", "Bearer " + token)
			        .content(generateRequest(getQuery, variables))
			        .contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn(); 
			
			String transResponse2 = mvc.perform(asyncDispatch(transResult2))
			        .andExpect(status().isOk())
			        .andReturn().getResponse().getContentAsString();
			
			assertFalse(isForbidden(transResponse2)); 
			JSONObject transJson2 = parseResponse(transResponse2); 
			Transaction trans2 = gson.fromJson(transJson2.getString("getTransactionById"), Transaction.class);
			 
			assertEquals(transId, trans2.getId()); 
			
			
		}catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		transRepo.deleteById(transId);
		
		//attempt to create an transaction from another account 
		
		variables.put("fromAccount", "2222"); 
		variables.put("toAccount", "1111"); 
		variables.put("value", 100);

		variables = new JSONObject(); 
		variables.put("id", transId); 
		
		MvcResult invalidResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(transQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		mvc.perform(asyncDispatch(invalidResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
	
		assertTrue(isForbidden( adminResponse )); 
	}
	
	  private String generateRequest(String query, JSONObject variables) throws JSONException {
		    JSONObject jsonObject = new JSONObject();
		    jsonObject.put("query", query);
		    if (variables != null) {
		      jsonObject.put("variables", variables);
		    }
		    return jsonObject.toString();
		  }
	  
	  private JSONObject parseResponse(String response) throws JSONException {
		  JSONObject obj = new JSONObject(response); 
		  
		 return  obj.getJSONObject("data"); 
		  
	  }
	  
	  private boolean isForbidden(String response) throws JSONException {
		  JSONObject obj = new JSONObject(response); 
		
		 try {
			 JSONArray jsons =   (JSONArray) obj.get("errors");
			 for(int i = 0; i < jsons.length(); i ++) {
				 JSONObject json = jsons.getJSONObject(i); 
				 if (json.getString("message").equals("Forbidden")) {
					 return true; 
				 }
			 }
			 return false; 

		 } catch (Exception e) {
			 return false; 
		 }
	  }
	
	@Test
	void restAdminTest() throws Exception {
		Gson gson = new Gson(); 
		// get authorized 
		
		String authQuery = "query GetAdmin($user : String ,  $pass : String){\n" + 
				"  \n" + 
				"	getAuthToken(username: $user, password: $pass)\n" + 
				"}"; 
	    JSONObject variables = new JSONObject();
	    variables.put("user", "admin"); 
	    variables.put("pass", "IAmAdmin");
	    
		MvcResult authResult = mvc.perform(post("/graphql")
		        .content(generateRequest(authQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String authResponse = mvc.perform(asyncDispatch(authResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		
		JSONObject data = parseResponse(authResponse); 
		String token = data.getString("getAuthToken"); 
		
		// get account details 
		String getAccountQuery = "query \n" + 
				"  GetAccount($accountNumber: String ){\n" + 
				"    getAccountByNumber(accountNumber: $accountNumber){\n" + 
				"    accountNumber, \n" + 
				"    value \n" + 
				"  	}\n" + 
				"  }"; 
	    variables = new JSONObject();
	    variables.put("accountNumber", "1111"); 

	    MvcResult accResult = mvc.perform(post("/graphql")
	    		.header("Authorization", "Bearer " + token)
		        .content(generateRequest(getAccountQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String accResponse = mvc.perform(asyncDispatch(accResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		JSONObject accData = parseResponse(accResponse).getJSONObject("getAccountByNumber"); 
		
		assertEquals("1111", accData.getString("accountNumber"));
		
		
		
		// attempt to send an admin request 
		String adminQuery = "query {\n" + 
				"  getAllAccounts{\n" + 
				"    accountNumber, \n" + 
				"    value\n" + 
				"  }\n" + 
				"}"; 
		
		variables = new JSONObject(); 
		MvcResult adminResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(adminQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String adminResponse = mvc.perform(asyncDispatch(adminResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
	
		assertFalse(isForbidden(adminResponse)); 
		
		JSONArray accounts = parseResponse(adminResponse).getJSONArray("getAllAccounts"); 
		assertNotEquals(0, accounts.length());
		
		
		
		// create a transaction 
		String transQuery = "query GetTrans($fromAccount: String!, $toAccount : String!, $value: Float){\n" + 
				"  createTransaction(fromAccount: $fromAccount, toAccount:$toAccount, value: $value){\n" + 
				"    id, \n" + 
				"    status, \n" + 
				"    fromAccount, \n" + 
				"    toAccount \n" + 
				"  }\n" + 
				"}"; 
		
		variables = new JSONObject(); 
		variables.put("fromAccount", "1111"); 
		variables.put("toAccount", "2222"); 
		variables.put("value", 100);
		
		MvcResult transResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(transQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String transResponse = mvc.perform(asyncDispatch(transResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		assertFalse(isForbidden(transResponse)); 
		JSONObject transJson = parseResponse(transResponse).getJSONObject("createTransaction"); 
		assertNotNull(transJson); 
		int transId = transJson.getInt("id"); 
		
		try {
			 
			assertEquals(transJson.getString("fromAccount"), "1111"); 
			assertEquals(transJson.getString("toAccount"), "2222"); 
			assertEquals(transJson.getString("status"), Transaction.NOTPROCCESSED); 
			
		}  catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		
		
		//get the transaction 
		try{
			
			String getQuery = "query GetTrans($id : Int!){\n" + 
					"  getTransactionById(id: $id){\n" + 
					"    fromAccount, \n" + 
					"    toAccount, \n" + 
					"    id, \n" + 
					"    value\n" + 
					"  }\n" + 
					"}"; 
			variables = new JSONObject(); 
			variables.put("id", transId); 
			
			MvcResult transResult2 = mvc.perform(post("/graphql")
					.header("Authorization", "Bearer " + token)
			        .content(generateRequest(getQuery, variables))
			        .contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn(); 
			
			String transResponse2 = mvc.perform(asyncDispatch(transResult2))
			        .andExpect(status().isOk())
			        .andReturn().getResponse().getContentAsString();
			
			assertFalse(isForbidden(transResponse2)); 
			JSONObject transJson2 = parseResponse(transResponse2); 
			Transaction trans2 = gson.fromJson(transJson2.getString("getTransactionById"), Transaction.class);
			 
			assertEquals(transId, trans2.getId()); 
			
			
		}catch(Exception e) {
			transRepo.deleteById(transId); 
			throw (e); 
		} catch (Error e) {
			transRepo.deleteById(transId);
			throw (e);
		}
		
		transRepo.deleteById(transId);
		
		
		// create an account 
		String createQuery = "query createAccountNew($accountNumber: String!, $value : Float){\n" + 
				"  createAccount(accountNumber: $accountNumber, value: $value){\n" + 
				"    accountNumber, \n" + 
				"    value \n" + 
				"  }\n" + 
				"}"; 
		variables = new JSONObject(); 
		variables.put("accountNumber", "666"); 
		variables.put("value", 666f); 
		
		
		MvcResult createResult = mvc.perform(post("/graphql")
				.header("Authorization", "Bearer " + token)
		        .content(generateRequest(createQuery, variables))
		        .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(); 
		
		String createResponse = mvc.perform(asyncDispatch(createResult))
		        .andExpect(status().isOk())
		        .andReturn().getResponse().getContentAsString();
		
		assertFalse(isForbidden(createResponse)); 
		JSONObject newAccount = parseResponse(createResponse).getJSONObject("createAccount"); 
		
		
		try {
			
			assertEquals(newAccount.getString("accountNumber"), "666"); 
			assertEquals(newAccount.getDouble("value"), 666, 0.5); 
			
		}catch (Exception e) {
			accountRepo.deleteByAccountNumber("666");
			throw(e); 
			
		} catch (Error e) {
			accountRepo.deleteByAccountNumber("666");
			throw(e); 
		}
	
	}
	
	

}
