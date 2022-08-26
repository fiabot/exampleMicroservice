package com.usbank.controllers;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usbank.Repositories.JdbcAccountManagementRepository;
import com.usbank.models.Account;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Set up REST request 
 * and link them to corresponding 
 * jbcdTemplate repository 
 * @author fmshyne
 *
 */

@RestController  
public class RestAccountMangementController   
{  
	
	@Autowired 
	JdbcAccountManagementRepository accRepo; 
	 
	
	
	/**
	 * Get all transactions from the SQL database 
	 * @return response entity with list of all transactions 
	 */
	@ApiOperation(value = "Get all accounts from database", nickname = "getAllAccounts", tags = {"SQLAccounts", })
	@RequestMapping(value = "/admin/getAllAccounts/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getAllAccounts(){
			
		List<Account> queryResult = accRepo.getAllAccounts(); 
		return new ResponseEntity<>(queryResult, HttpStatus.OK);
		
	}
	
	/**
	 * get account with given account number 
	 * @param accountNumber string representation of account number
	 * @return response entity with account 
	 */
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #accountNumber)") // users can only access their account 
	@ApiOperation(value = "Get an Account from number", nickname = "getAccountByNumber", tags = {"SQLAccounts", })
	@RequestMapping(value = "/user/getAccountByNumber/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getAccountByNumber(
			@ApiParam(value = "Account Number") @RequestParam(value = "accountNumber", required = true) String accountNumber){
			
		List<Account> queryResult = accRepo.getAccount(accountNumber); 
		return new ResponseEntity<>(queryResult, HttpStatus.OK);
		
	}
	
	
	
	/***
	 * create an account and post 
	 * it to sql database 
	 * @param accountNumber number of new account 
	 * @param value starting value of account 
	 * @return response entity with account created 
	 */
	@ApiOperation(value = "Post a new account to the sql database", nickname = "createAccount", tags = {"SQLAccounts", })
	@RequestMapping(value = "/admin/createAccount/", produces = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<?> createAccount(
			@ApiParam(value = "Account Number") @RequestParam(value = "accountNumber", required = true) String accountNumber, 
			@ApiParam(value = "Account Value") @RequestParam(value = "value", required = true) double value){
			
		Account acc = accRepo.postAccount(accountNumber, value); 
		return new ResponseEntity<>(acc, HttpStatus.OK);
		
	}
	 
}  