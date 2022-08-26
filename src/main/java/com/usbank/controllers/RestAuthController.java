package com.usbank.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


import com.usbank.security.JwtResponse;
import com.usbank.security.JwtUserDetailsService;
import com.usbank.security.JwtUtil;


/**
 * 
 * Create JwtTokens using REST requests 
 * @author fmshyne and Kajal Rawal
 *
 */
@RestController
@CrossOrigin
public class RestAuthController {

	@Autowired
	private AuthenticationManager authenticationManager; //cofigured in web security config 

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;
	
	/**
	 * Create a new JwtToken given login information 
	 * @param username name of user 
	 * @param password credentials of user 
	 * @return jwtToken as string 
	 * @throws Exception if login information cannot be validated 
	 */
	@ApiOperation(value = "Create a new Auth token", nickname = "createAuthenticationToken", tags = {"Auth", })
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(
			@ApiParam(value = "Username") @RequestParam(value = "username", required = true) String username,
			@ApiParam(value = "Password") @RequestParam(value = "password", required = true) String password
			) throws Exception {
		
		try {
			authenticate(username, password);

			
		} catch (Exception e) {
			return new ResponseEntity<>("Authentication Failed", HttpStatus.UNAUTHORIZED);
			
		}
		
		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(username);

		final String token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new JwtResponse(token));
	
		
	}
	
	/**
	 * Throw an exception if login 
	 * information does not match
	 * authentication manager 
	 * @param username name of user 
	 * @param password credentials of user 
	 * @throws Exception is user is not accessible with given information 
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