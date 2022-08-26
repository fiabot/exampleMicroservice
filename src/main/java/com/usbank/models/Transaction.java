package com.usbank.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;




/**
 * A transaction element, 
 * 
 * used in two main ways: 
 * 		matching grapgql schemas: 
 * 			- the parameters must match whats in the
 * 			 schema for account in resources/graphql/schemas 
 * 		matching sql database: 
 * 			- the @entity tag tells jpa that this should be a table 
 * 			- if "Transactions" does not exist in database, jpa creates a new one 
 * 			- parameters become columns as specified in tags 
 * @author fmshyne
 *
 */

@Entity(name = "Transactions")
public class Transaction implements Serializable {
	public static String NOTPROCCESSED = "NOT PROCESSED"; 
	public static String PROCCESSED = "PROCESSED"; 
	
	@Column(name = "from_account")
	String fromAccount; 
	
	@Column(name = "to_account")
	String toAccount; 
	double value; 
	
	String status; 
	
	// make sure that transactions cannot be created with 
	// non existent from or two account 
	@ManyToOne
	@JoinColumn(name = "from_account",  insertable=false, updatable=false, nullable = false)
	public Account fromAccountObj; 
	
	@ManyToOne
	@JoinColumn(name = "to_account",  insertable=false, updatable=false, nullable = false)
	Account toAccountObj; 
	
	@Id 
	@GeneratedValue 
	int id; 
	
	public Transaction() {
		super(); 
	}
	/**
	 * only used in mocked repo
	 */
	public Transaction(int id, String fromAccount, String toAccount, double amount) {
		super();
		this.id = id; 
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.value = amount;
		this.status = NOTPROCCESSED; 
	}
	
	public Transaction(String fromAccount, String toAccount, double amount) {
		super();
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.value = amount; 
		this.status = NOTPROCCESSED; 
	}


	

	public String getFromAccount() {
		return fromAccount;
	}


	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}


	public String getToAccount() {
		return toAccount;
	}


	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}


	public double getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Account getFromAccountObj() {
		return fromAccountObj;
	}
	public void setFromAccountObj(Account fromAccountObj) {
		this.fromAccountObj = fromAccountObj;
	}
	public Account getToAccountObj() {
		return toAccountObj;
	}
	public void setToAccountObj(Account toAccountObj) {
		this.toAccountObj = toAccountObj;
	}
}
