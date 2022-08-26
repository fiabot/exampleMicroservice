package com.usbank.controllers;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.usbank.Repositories.JdbcTransactionManagementRepository;
import com.usbank.models.Transaction;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Set up REST request 
 * and link them to corresponding 
 * jdbcTempalte repository
 * 
 *  can be accessed at localhost:8080/swagger-ui
 * @author fmshyne
 *
 */

@RestController  
public class RestTransactionMangementController   
{  
	
	@Autowired 
	JdbcTransactionManagementRepository transRepo; 
	 
	
	
	/**
	 * Get all transactions from the SQL database 
	 * @return list of all transactions as a response entity
	 */
	@ApiOperation(value = "Get all transactions from database", nickname = "getAllTransaction", tags = {"SQLTransaction", })
	@RequestMapping(value = "/admin/getAllTransactions/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getAllTransactions(){
			
		List<Transaction> queryResult = transRepo.getAllTransactions(); 
		return new ResponseEntity<>(queryResult, HttpStatus.OK);
		
	}
	
	/**
	 *Get all incoming transactions from the SQL database 
	 * @param toAccount account number for query  
	 * @return list of all transactions as a response entity
	 */
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #toAccount)") // users can only access their own account 
	@ApiOperation(value = "Get all incoming transactions for account", nickname = "getIncomingTransaction", tags = {"SQLTransaction", })
	@RequestMapping(value = "/user/getIncomingTransaction/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getIncomingTransactions(
			@ApiParam(value = "Account number") @RequestParam(value = "toAccount", required = true) String toAccount){
			
		List<Transaction> queryResult = transRepo.getIncomingTransactions(toAccount); 
		return new ResponseEntity<>(queryResult, HttpStatus.OK);
		
	}
	
	/**
	 * Get all outgoing transactions from the SQL database 
	 * @param fromAccount account number for query 
	 * @return list of all transactions as a response entity
	 */
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #fromAccount)") // users can only access their own account 
	@ApiOperation(value = "Get all outgoing transactions for account", nickname = "getOutgoingTransaction", tags = {"SQLTransaction", })
	@RequestMapping(value = "/user/getOutgoingTransaction/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getOutgoingTransactions(
			@ApiParam(value = "Account number") @RequestParam(value = "fromAccount", required = true) String fromAccount){
			
		List<Transaction> queryResult = transRepo.getOutgoingTransactions(fromAccount); 
		return new ResponseEntity<>(queryResult, HttpStatus.OK);
		
	}
	
	/**
	 * Get all outgoing transactions from the SQL database 
	 * @param fromAccount account number for query 
	 * @return list of all transactions as a response entity
	 */
	@PostAuthorize("hasAnyAuthority('admin', 'ACC_' + returnObject.getBody().get(0).fromAccount, 'ACC_' + returnObject.getBody().get(0).toAccount)") // only get transactions related to account 
	@ApiOperation(value = "Get transaction with id number", nickname = "getTransactionById", tags = {"SQLTransaction", })
	@RequestMapping(value = "/user/getTransactionById/", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<?> getTransactionById(
			@ApiParam(value = "Id number") @RequestParam(value = "id", required = true) int id){
			
		List<Transaction> queryResult = transRepo.getTransactionFromId(id); 
		
		return new ResponseEntity<>(queryResult, HttpStatus.OK);
		
	}
	
	
	
	/**
	 * Add a transaction to database 
	 * @param fromAccount account sending money
	 * @param toAccount account receiving money 
	 * @param value value being transfered 
	 * @return created transaction as a response entity
	 */
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #fromAccount)") // only senders can create transactions 
	@ApiOperation(value = "Create and intsert a new transaction to the sql database", nickname = "createTransaction", tags = {"SQLTransaction", })
	@RequestMapping(value = "/user/createTransaction/", produces = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<?> createTransaction(
			@ApiParam(value = "From Account") @RequestParam(value = "fromAccount", required = true) String fromAccount, 
			@ApiParam(value = "To Account") @RequestParam(value = "toAccount", required = true) String toAccount,
			@ApiParam(value = "Transaction Value") @RequestParam(value = "value", required = true) double value){
			
		Transaction trans = transRepo.postTransaction(fromAccount, toAccount, value); 
		return new ResponseEntity<>(trans, HttpStatus.OK);
		
	}
	
	
	 
}  