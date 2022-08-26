package com.usbank.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.usbank.models.Transaction;

/**
 * JPA can automatically create CRUD operations 
 * for our SQL database, since Transaction is a entity 
 * object 
 * @author fmshyne
 *
 */
public interface JpaTransactionRepository extends JpaRepository<Transaction, Integer> {
	
	/**
	 * Get the transactions sent from 
	 * a specified account number 
	 * 
	 * this uses JPA naming system
	 * to automatically implement this 
	 * query 
	 * @param fromAccount account sending transactions 
	 * @return list of transactions sent from fromAccount 
	 */
	public List<Transaction> findByFromAccount(String fromAccount); 
	
	/**
	 * Get the transactions received by  
	 * a specified account number 
	 * 
	 * this uses JPA naming system
	 * to automatically implement this 
	 * query 
	 * @param toAccount account receiving transactions 
	 * @return list of transactions received by toAccount 
	 */
	public List<Transaction> findByToAccount(String toAcconut); 
	
	/**
	 * Change the status of an transaction
	 * 
	 * A custom query is injected to this method 
	 * @param id id of transaction to change 
	 * @param status new status of transaction
	 */
	@Modifying 
	@Transactional 
	@Query("update Transactions t set t.status = :status where t.id = :id")
	void updateStatus(@Param(value = "id") int id, @Param(value = "status") String status);
	
}
