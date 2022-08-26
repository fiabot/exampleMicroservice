package com.usbank.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import com.usbank.Repositories.JpaAccountRepository;
import com.usbank.Repositories.JpaTransactionRepository;
import com.usbank.models.Account;
import com.usbank.models.Transaction;
import com.usbank.security.JwtUserDetailsService;
import com.usbank.security.JwtUtil;

/**
 * Controller for implemented the schemas as listed 
 * in resources/graphQL/schemas 
 * 
 * Controllers can be accessed on http://localhost:8080/graphiql/
 * @author fmshyne
 *
 */
@Controller 
public class GraphQLController {
    
	
	@Autowired 
	JpaAccountRepository jpaAccountRepo; 
	
	@Autowired 
	JpaTransactionRepository jpaTransRepo; 
	
	@Autowired
	private AuthenticationManager authenticationManager; //cofigured in web security config 
	
	@Autowired
	private JwtUserDetailsService userDetailsService;
	
	@Autowired
	private JwtUtil jwtTokenUtil;
	
	/**
	 * return all current accounts 
	 * @return list of accounts 
	 */
	@PreAuthorize("hasAuthority('admin')")
	@QueryMapping 
	public List<Account> getAllAccounts(){
		return jpaAccountRepo.findAll(); 
	}
	
	/**
	 * return all current transactions 
	 * @return list of transactions 
	 */
	@PreAuthorize("hasAuthority('admin')") 
	@QueryMapping 
	public List<Transaction> getAllTransactions(){
		return jpaTransRepo.findAll();  
	}
	
	/**
	 * return the account with 
	 * the associated number 
	 * 
	 * can cause problems if account 
	 * numbers are not unique values 
	 * as defined in the sql table 
	 * 
	 * Returns null if account is not found 
	 * 
	 * @param accountNumber string of account number 
	 * @return account with given account number 
	 */
	@QueryMapping 
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #accountNumber)") 
	public Account getAccountByNumber (@Argument String accountNumber) {
		try {
			Account account =  jpaAccountRepo.findByAccountNumber(accountNumber).get(0); 
			System.out.println(account.incomingTransactions); 
			return account; 
		} catch (Exception e){ 
			return null; 
		}
		

	}
	
	/**
	 * get a transaction by it's 
	 * id number 
	 * 
	 * return null if not found 
	 * @param id int value of id 
	 * @return transaction wit given id number 
	 */
	@PostAuthorize("hasAnyAuthority('admin', 'ACC_' + returnObject.fromAccount, 'ACC_' + returnObject.toAccount)") // only get transactions related to account 
	@QueryMapping 
	public Transaction getTransactionById(@Argument int id) {
		Optional<Transaction> trans = jpaTransRepo.findById(id); 
		
		if(trans.isPresent()) {
			return trans.get(); 
		}else {
			return null; 
		}
	}
	
	/**
	 * create a new account and 
	 * post to sql table 
	 * 
	 * Returns null if account already 
	 * exists, and does not create new one
	 * @param accountNumber number of new account, must not already exist
	 * @param value starting value of account 
	 * @return account generated 
	 */
	@PreAuthorize("hasAuthority('admin')")
	@QueryMapping
	public Account createAccount (@Argument String accountNumber, @Argument float value) {
		// dont add duplicate account 
		if (getAccountByNumber(accountNumber) != null) {
			return null; 
		} else {
			Account acc = new Account(accountNumber, value); 
			return jpaAccountRepo.save(acc);
		}
		
	}
	
	/**
	 * create a new transaction 
	 * @param fromAccount account sending transaction 
	 * @param toAccount account receiving transaction 
	 * @param value value of transaction 
	 * @return transaction generated 
	 */
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #fromAccount)") // only senders can create transactions 
	@QueryMapping
	public Transaction createTransaction(@Argument String fromAccount, @Argument String toAccount, @Argument float value) {
		try {
			Transaction trans = new Transaction(fromAccount, toAccount, value); 
			return jpaTransRepo.save(trans);  
		} catch (IllegalArgumentException e){
			throw e; 
		}
		
	} 
	
	/**
	 * Delete a transaction by id, 
	 * must be admin 
	 * @param transactionId id number for transaction 
	 * @return true if deletion was successful 
	 */
	@PreAuthorize("hasAuthority('admin')")
	@QueryMapping 
	public boolean deleteTransaction(@Argument int transactionId) {
		jpaTransRepo.deleteById(transactionId);
		return true; 
	}
	
	/**
	 * Delete account by account number 
	 * @param accountNumber string value of account number 
	 * @return true if deletion was successful 
	 */
	@PreAuthorize("hasAuthority('admin')")
	@QueryMapping 
	public boolean deleteAccount(@Argument String accountNumber) {
		jpaAccountRepo.deleteByAccountNumber(accountNumber);
		return true; 
	}
	
	/**
	 * Change the account value of an account, 
	 * must be admin  
	 * @param accountNumber string value of account number 
	 * @param value value to change account to 
	 * @return account that has been modified 
	 */
	@PreAuthorize("hasAuthority('admin')")
	@QueryMapping Account updateAccountValue(@Argument String accountNumber, @Argument double value) {
		jpaAccountRepo.updateValue(accountNumber, value);
		return getAccountByNumber(accountNumber); 
	}
	
	/**
	 * Process an unproccessed transaction
	 *  by removing funds from fromAccount and adding 
	 *  funds to toAccount. 
	 *  
	 *  must have access to fromAccount or be admin 
	 * @param transactionId id of transaction to process 
	 * @param fromAccount account sending funds 
	 * @param toAccount account recieving funds 
	 * @return processed transaction 
	 * @throws Exception throws exception if cannot validate transaction 
	 */
	@PreAuthorize("hasAnyAuthority('admin', 'ACC_' + #fromAccount)") // only senders can proccess transactions 
	@QueryMapping Transaction processTransaction(@Argument int transactionId, @Argument String fromAccount, @Argument String toAccount) throws Exception{
		Transaction trans = getTransactionById(transactionId); 
		// only process if not already processed 
		if (validateTransaction(trans, fromAccount, toAccount)) {
			// take from giving account 
			Account fromAcc = getAccountByNumber(trans.getFromAccount()); 
			jpaAccountRepo.updateValue(fromAcc.accountNumber, fromAcc.getValue() - trans.getValue()); 
			
			//give to to account
			Account toAcc = getAccountByNumber(trans.getToAccount()); 
			jpaAccountRepo.updateValue(toAcc.accountNumber, toAcc.getValue() + trans.getValue());
			
			//update status 
			jpaTransRepo.updateStatus(trans.getId(), Transaction.PROCCESSED);
			return getTransactionById(transactionId); //get updated transaction 
		}else {
			throw new Exception("Transaction Validation Failed"); 
		}
	}
	
	/**
	 * Create a JwtToken for a user 
	 * given login information 
	 * @param username name of user 
	 * @param password password of user 
	 * @return jwtToken as string 
	 * @throws Exception throws exception if user cannot be validated 
	 */
	@QueryMapping 
	String getAuthToken (@Argument String username, @Argument String password) throws Exception {
		try {
			authenticate(username, password);

			
		} catch (Exception e) {
			throw new Exception(e);
			
		}
		
		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(username);

		final String token = jwtTokenUtil.generateToken(userDetails);

		return token;
	}
	
	// Schema mappings are provided, but commented out as 
	// JPA does this for us 
	
	/**
	 * map incoming transactions with given 
	 * account, with this graphQL will connect
	 * account and incoming transaction  
	 * @param account account to find transactions received 
	 * @return list of transactions that this account received 
	 */
	/*@SchemaMapping
	public List<Transaction> incomingTransactions (Account account){
		return jpaTransRepo.findByToAccount(account.accountNumber); 
	}*/
	
	/**
	 * map outgoing transactions with given 
	 * account, with this graphQL will connect
	 * account and outgoing transaction  
	 * @param account account to find transactions given
	 * @return list of transactions that this account gave
	 */
	/*@SchemaMapping
	public List<Transaction> outgoingTransactions ( Account account){
		return jpaTransRepo.findByFromAccount(account.accountNumber); 


	}*/
	
	/**
	 * Return true if provided info matches transaction 
	 * and transaction is not already processed 
	 * @param trans transaction to validate 
	 * @param fromAccount account sending funds 
	 * @param toAccount account receiving funds 
	 * @return true if transaction can be processed 
	 */
	private boolean validateTransaction(Transaction trans, String fromAccount, String toAccount) {
		return trans.getStatus().contentEquals(Transaction.NOTPROCCESSED) && trans.getFromAccount().equals(fromAccount) && trans.getToAccount().equals(toAccount); 
	}
	
	/**
	 * Ensure username and password matches 
	 * what is expected from authentication manager 
	 * @param username name of user 
	 * @param password password of user 
	 * @throws Exception when login information cannot be validated with authentication manager 
	 */
	private void authenticate(String username, String password) throws Exception {
		try {
			
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}