package com.usbank.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * An account element 
 * 
 * used in two main ways: 
 * 		matching grapgql schemas: 
 * 			- the parameters must match whats in the
 * 			 schema for account in resources/graphql/schemas 
 * 		matching sql database: 
 * 			- the @entity tag tells jpa that this should be a table 
 * 			- if "accounts" does not exist in database, jpa creates a new one 
 * 			- parameters become columns as specified in tags   
 * @author fmshyne
 *
 */
@Entity(name="accounts")
public class Account implements Serializable {
	
	@Column(unique = true)
	@Id 
	public String accountNumber; 
	double value; 
	
	@OneToMany()
	@JoinColumn(name = "from_account", referencedColumnName = "accountNumber")
	public List<Transaction> outgoingTransactions; 
	
	@OneToMany()
	@JoinColumn(name = "to_account", referencedColumnName = "accountNumber")
	public
	List<Transaction> incomingTransactions; 
	
	/**
	 * only used in mock repo
	 */
	public Account(int id, String account, double value) {
		super(); 
		this.accountNumber = account;
		outgoingTransactions = new ArrayList<Transaction>(); 
		incomingTransactions = new ArrayList<Transaction>(); 
		this.value = value; 
	}
	
	/**
	 * create an account with number and starting value 
	 * @param account account number 
	 * @param value starting value of accoutn 
	 */
	public Account(String account, double value) {
		
		this.value = value; 
		this.accountNumber = account; 
		//nextId ++; 
	}
	
	/**
	 * default constructor needed 
	 * for JPA to create 
	 * new object 
	 */
	public Account() {
		super(); 
	}
	
	/**
	 * Add an incoming transaction 
	 * 
	 * only used in mock repo
	 * @param trans transaction to add 
	 */
	public void addIncomingTransaction(Transaction trans) {
		incomingTransactions.add(trans); 
	}
	
	/**
	 * add an outgoing transaction  
	 * 
	 * only used in mock repo 
	 * @param trans transaction to add 
	 */
	public void addOutgoingTransaction(Transaction trans) {
		outgoingTransactions.add(trans); 
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}


	public List<Transaction> getOutgoingTransactions() {
		return outgoingTransactions;
	}

	public void setOutgoingTransactions(List<Transaction> outgoingTransactions) {
		this.outgoingTransactions = outgoingTransactions;
	}

	public List<Transaction> getIncomingTransactions() {
		return incomingTransactions;
	}

	public void setIncomingTransactions(List<Transaction> incomingTransactions) {
		this.incomingTransactions = incomingTransactions;
	}
}
