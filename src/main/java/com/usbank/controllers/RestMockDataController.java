package com.usbank.controllers;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usbank.Repositories.MockDataRepository;
import com.usbank.models.Account;
import com.usbank.models.Transaction;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
  

/**
 * Set up REST request 
 * and link them to mocked 
 * repository 
 * 
 * mocked repo contains a internal 
 * list of accounts and 
 * transactions, does not connect 
 * to database 
 * 
 * can be accessed at localhost:8080/swagger-ui/
 * @author fmshyne
 *
 */
//@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-09-23T13:18:04.838-05:00[America/Chicago]")
@Api(value = "usb", description = "the usb API")
@RestController  
public class RestMockDataController   
{  
	@Autowired 
	MockDataRepository repo; //impl in repositories package 

	
	/**
	 * Get an account from mocked database  
	 * @param account account number to retrieve 
	 * @return response entity of account and corresponding http status 
	 */
	@ApiOperation(value = "Get an account from a account number", nickname = "getAccount", tags = {"MockAccounts", })
	@RequestMapping(value = "/getAccount/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getAccount(
			@ApiParam(value = "Account Number") @RequestParam(value = "account", required = true) String account){
		
		Account acc = repo.getAccount(account); 
		if(acc == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}else {
			return new ResponseEntity<>(acc, HttpStatus.OK); 
		}
		
		
	}
	
	/**
	 * Create a new account and add it to 
	 * mocked database 
	 * @param account account number 
	 * @param value starting value of account 
	 * @return created account as a response entity
	 */
	@ApiOperation(value = "Post a new account given a number", nickname = "postAccount", tags = {"MockAccounts", })
	@RequestMapping(value = "/postAccount/", produces = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<?> postAccount(
			@ApiParam(value = "Account Number") @RequestParam(value = "account", required = true) String account, 
			@ApiParam(value = "Account Value") @RequestParam(value = "value", required = true) double value){
		
		Account acc = repo.createAccount(account, value); 
		
		if (acc == null) {
			new ResponseEntity<>("Recourse already exists", HttpStatus.NOT_ACCEPTABLE); 
		}
		return new ResponseEntity<>(acc, HttpStatus.OK);
		
	}
	
	/**
	 * Add transaction to mocked database 
	 * links transaction to accounts, and 
	 * updates values 
	 * @param incoming account which is receiving money  
	 * @param outgoing account which is giving money
	 * @param value value being transfered 
	 * @return resulting transaction in a response entity
	 */
	@ApiOperation(value = "Post a new transaction given incoming and outgoing accounts", nickname = "postTransaction", tags = {"MockTransaction", })
	@RequestMapping(value = "/postTransaction/", produces = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<?> postTransaction(
			@ApiParam(value = "Incoming Account Number") @RequestParam(value = "incoming", required = true) String incoming, 
			@ApiParam(value = "Outgoing Account Number") @RequestParam(value = "outgoing", required = true) String outgoing,
			@ApiParam(value = "Account Value") @RequestParam(value = "value", required = true) double value){
		
		Transaction trans = repo.postTransaction(incoming, outgoing, value); 
		
		if (trans == null) {
			new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND); 
		}
		return new ResponseEntity<>(trans, HttpStatus.OK);
		
	}
	

	 
}  