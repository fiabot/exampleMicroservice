package com.usbank.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.usbank.models.Account;

/**
 * implements basic CRUD operations for account 
 * 
 * Since account is an entity, JpaRepository knows 
 * how to create sql calls, so we don't have to worry about 
 * 
 * the id value of account is a float 
 * @author fmshyne
 *
 */
@Repository
public interface JpaAccountRepository extends JpaRepository<Account, String> {
	
	/**
	 * The naming system findBy... 
	 * is used to make JPA 
	 * implement queries for us 
	 * 
	 * Here JPA will return 
	 * a list of accounts 
	 * given an account number 
	 * 
	 * @param AccountNumber account number of account to find 
	 * @return list of accounts with matching number 
	 */
	List<Account> findByAccountNumber(String accountNumber);
	
	/**
	 * Delete all accounts with 
	 * given an account number 
	 * 
	 * Uses JPA naming system so that 
	 * JPA will implement this for us
	 * @param accountNumber account number to delete 
	 */
	@Transactional
	void deleteByAccountNumber(String accountNumber); 
	
	/**
	 * Modify an account value 
	 * 
	 * More complicated queries cannot 
	 * be done automatically, so we 
	 * have to add query specifically 
	 * @param accountNumber number of account modify 
	 * @param value new value of account 
	 */
	@Modifying 
	@Transactional 
	@Query("update accounts a set a.value = :value where a.accountNumber = :accountNumber")
	void updateValue(@Param(value = "accountNumber") String accountNumber, @Param(value = "value") double value);
}
