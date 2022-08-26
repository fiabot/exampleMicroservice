package com.usbank.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * A mock service for getting user details 
 * 
 * contains mock accounts for: 
 * 	admin
 * 	user1
 *  user2 
 * @author fmshyne
 *
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {
	public static String ADMIN_AUTH = "admin"; 
	public static String USER_AUTH = "user";
	
	/**
	 * Mock database of users 
	 * @return a hash map with username and associated users 
	 */
	private Map<String, User> getUsers(){
		Map<String, User> users = new HashMap<String, User>();
		
		List<GrantedAuthority> user1Auth = new ArrayList<>();  
		List<GrantedAuthority> user2Auth = new ArrayList<>(); 
		List<GrantedAuthority> adminAuth = new ArrayList<>();  
		adminAuth.add(new SimpleGrantedAuthority (ADMIN_AUTH)); 
		adminAuth.add(new SimpleGrantedAuthority (USER_AUTH));
		
		user1Auth.add(new SimpleGrantedAuthority (USER_AUTH));
		user1Auth.add(new SimpleGrantedAuthority ("ACC_1111")); //allow access to speficic account 
		
		user2Auth.add(new SimpleGrantedAuthority (USER_AUTH));
		user2Auth.add(new SimpleGrantedAuthority ("ACC_2222")); //allow access to speficic account 
		
		
		User admin = new User("admin", "IAmAdmin", adminAuth); 
		User user1 = new User("user1", "IAmUser1", user1Auth); 
		User user2 = new User("user2", "IAmUser2", user2Auth);
		
		users.put("admin", admin); 
		users.put("user1", user1); 
		users.put("user2", user2); 
		
		return users;
	}
	
	/**
	 * Load user from username 
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Map<String, User> users = getUsers(); 
		if(users.containsKey(username)) {
			return users.get(username); 
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}
}
