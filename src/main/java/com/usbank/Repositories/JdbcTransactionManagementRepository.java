package com.usbank.Repositories;

import java.math.BigInteger;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import com.usbank.models.Transaction;

/**
 * Direct calls to the mySql database
 * for "Transactions" will be done here 
 * 
 * Information about sql database 
 * can be found in application.properties 
 * @author fmshyne
 *
 */
@Repository 
public class JdbcTransactionManagementRepository {
	

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	/**
	 * select all transactions from table 
	 * @return list of transactions 
	 */
	public List<Transaction> getAllTransactions(){
		BeanPropertyRowMapper<Transaction> mapper = new BeanPropertyRowMapper<Transaction>(Transaction.class); 
		List<Transaction> rows = jdbcTemplate.query("SELECT * from transactions", mapper);
				
		return rows; 

	}
	
	/**
	 * get incoming transactions for a specific account 
	 * @param toAccount account number account receiving transactions 
	 * @return list of transactions where toAccount is the receiving party 
	 */
	public List<Transaction> getIncomingTransactions(String toAccount) {
		BeanPropertyRowMapper<Transaction> mapper = new BeanPropertyRowMapper<Transaction>(Transaction.class); 
		String query = String.format("Select * from transactions where to_account = '%s'", toAccount); 
		
		List<Transaction> rows = jdbcTemplate.query(query, mapper);
		return rows; 
	}
	
	/**
	 * get outgoing transactions for a specific account 
	 * @param fromAccount account number account giving transactions 
	 * @return list of transactions where toAccount is the giving party 
	 */
	public List<Transaction> getOutgoingTransactions(String fromAccount) {
		BeanPropertyRowMapper<Transaction> mapper = new BeanPropertyRowMapper<Transaction>(Transaction.class); 
		String query = String.format("Select * from transactions where from_account = '%s'", fromAccount); 
		
		List<Transaction> rows = jdbcTemplate.query(query, mapper);
		return rows;
	}
	
	/**
	 * get a transaction given id number 
	 * @param id id of transaction 
	 * @return list containing (hopefully) one transaction  
	 */
	public List<Transaction> getTransactionFromId(int id) {
		BeanPropertyRowMapper<Transaction> mapper = new BeanPropertyRowMapper<Transaction>(Transaction.class); 
		String query = String.format("Select * from transactions where id = '%d'", id); 
		
		List<Transaction> rows = jdbcTemplate.query(query, mapper);
		return rows; 
 
	}
	
	/**
	 * create new transaction and insert it 
	 * into Transaction table 
	 * @param fromAccount account number of receiving account 
	 * @param toAccount account number of giving account 
	 * @param value value of transaction 
	 * @return new transaction created 
	 */
	public Transaction postTransaction(String fromAccount, String toAccount, double value) {
		String query = String.format("Insert Into transactions (from_account, to_account, value, status ) values ('%s', '%s', %f, '%s')", fromAccount, toAccount, value, Transaction.NOTPROCCESSED);
		jdbcTemplate.execute(query); 
		
		// get id value of new transaction 
		
		Map<String, Object> map = jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID();"); 
		int id = ((BigInteger) map.get("LAST_INSERT_ID()")).intValue(); 
		
		//create transaction object 
		Transaction trans = new Transaction(id, fromAccount, toAccount, value); 
		
		return trans;
		
	}
	
	/**
	 * Delete a transaction from the database
	 * given an id 
	 * @param id id of transaction 
	 */
	public void deleteTransactionById(int id) {
		//insert new account 
		String query = String.format("DELETE FROM transactions where id = %d", id);
		jdbcTemplate.execute(query); 
		
	}
}
