package com.usbank.security;

import java.io.Serializable;

/**
 * Response class to send 
 * jwtToken
 *
 */
public class JwtResponse implements Serializable {

	private final String jwttoken;

	public JwtResponse(String jwttoken) {
		this.jwttoken = jwttoken;
	}

	public String getToken() {
		return this.jwttoken;
	}
}