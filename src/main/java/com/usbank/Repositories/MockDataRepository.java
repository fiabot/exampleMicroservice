package com.usbank.Repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.usbank.models.Account;
import com.usbank.models.Transaction;

/**
 * A mocked database where 
 * the data is stored in an ArrayList 
 * @author fmshyne
 *
 */
@Repository 
public class MockDataRepository {
	List<Account> accounts; 
	int nextAccountID =0; 
	int nextTransID = 0; 

	/**
	 * Create respo with 
	 * empty list 
	 */
	public MockDataRepository() {
		super(); 
		accounts = new ArrayList<Account>(); 
	}
	
	/**
	 * Get account given account number
	 * 
	 * Return null if not found 
	 * @param account number of account 
	 * @return account object of account
	 */
	public Account getAccount(String account) {
		for(Account acc : accounts){
			if (acc.accountNumber.equals(account)){
				return acc; 
			}
		}
		
		return null; 
	}
	
	/**
	 * Create a new account 
	 * and add it to arraylist 
	 * @param account account number for account 
	 * @param value starting fund of account 
	 * @return the account that was created 
	 */
	public Account createAccount(String account, double value) {
		
		if(getAccount(account) != null) {
			return null;
		}else {
			Account acc = new Account(nextAccountID, account, value); 
			accounts.add(acc); 
			nextAccountID ++; 
			return acc; 
		}
	}
	
	/**
	 * Create a transaction 
	 * and add it to the outgoing 
	 * and incoming accounts 
	 * @param incomingAccount account receiving funds 
	 * @param outgoingAccount account sending funds 
	 * @param value value of transaction 
	 * @return transaction created 
	 */
	public Transaction postTransaction(String incomingAccount, String outgoingAccount, double value) {
		
		Account incoming = getAccount(incomingAccount); 
		Account outgoing = getAccount(outgoingAccount); 
		
		if(incoming == null || outgoing == null) {
			return null; // can't add transaction if accounts don't exist 
		}else {
			Transaction trans = new Transaction(nextTransID, outgoingAccount, incomingAccount, value); 
			incoming.addIncomingTransaction(trans);
			outgoing.addOutgoingTransaction(trans);
			nextTransID ++; 
			return trans; 
		}
		
		
	}
}
