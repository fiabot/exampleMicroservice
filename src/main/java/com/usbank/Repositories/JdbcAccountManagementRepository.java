package com.usbank.Repositories;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import com.usbank.models.Account;
import com.usbank.models.Transaction;

/**
 * Direct calls to the mySql database
 * for "Accounts" will be done here 
 * 
 * Information about sql database 
 * can be found in application.properties 
 * @author fmshyne
 *
 */
@Repository 
public class JdbcAccountManagementRepository {
	List<Account> accounts; 
	

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired 
	JdbcTransactionManagementRepository transRepo; 
	
	/**
	 * Automatically links accounts 
	 * and transactions 
	 * 
	 * not used in jdbcTemplate 
	 * @param accounts
	 * @return
	 */
	private List<Account> addTransactionsToAccounts(List<Account> accounts){
		for(Account account : accounts) {
			List<Transaction> incomingTransactions = transRepo.getIncomingTransactions(account.accountNumber); 
			List<Transaction> outgoingTransactions = transRepo.getOutgoingTransactions(account.accountNumber);
			
			account.setIncomingTransactions(incomingTransactions);
			account.setOutgoingTransactions(outgoingTransactions);
		}
		
		return accounts; 
	}
	
	/**
	 * return a list of all accounts in
	 * sql repo 
	 * @return list of accounts 
	 */
	public List<Account> getAllAccounts(){
		BeanPropertyRowMapper<Account> mapper = new BeanPropertyRowMapper<Account>(Account.class); 
		List<Account> rows = jdbcTemplate.query("SELECT * from accounts", mapper);
		//rows = addTransactionsToAccounts(rows); 
		return rows; 
		
	}
	
	/**
	 * select from accounts where account_number = accountNumber 
	 * @param accountNumber number of account to find 
	 * @return list of (hopefully) one account with account number 
	 */
	public List<Account> getAccount(String accountNumber) {
		BeanPropertyRowMapper<Account> mapper = new BeanPropertyRowMapper<Account>(Account.class);  
		String query = String.format("Select * from accounts where account_number = '%s'", accountNumber); 
		
		List<Account> rows = jdbcTemplate.query(query, mapper);
		//rows = addTransactionsToAccounts(rows); 
		return rows; 
	}
	
	
	/**
	 * create a new account and post it 
	 * to sql table 
	 * @param accountNumber account number of new account 
	 * @param value starting value of account 
	 * @return account created 
	 */
	public Account postAccount(String accountNumber, double value) {
		//insert new account 
		String query = String.format("Insert Into Accounts (account_number,  value) values ('%s',  %f)", accountNumber, value);
		jdbcTemplate.execute(query); 
		
		// get id value of new transaction 
		
		Map<String, Object> map = jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID();"); 
		int id = ((BigInteger) map.get("LAST_INSERT_ID()")).intValue(); 
		
		//create transaction object 
		Account account = new Account(id, accountNumber, value); 
		
		
		return account; 

		
	}
	
	/**
	 * Delete an account from the 
	 * database given the account number
	 * @param accountNumber number of account 
	 */
	public void deleteAccountByNumber(String accountNumber) {
		//insert new account 
		String query = String.format("DELETE FROM Accounts where account_number = '%s'", accountNumber);
		jdbcTemplate.execute(query); 
		
	}
}
