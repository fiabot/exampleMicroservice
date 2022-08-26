package com.usbank.security;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Utilities for creating and 
 * interpeting jwtTokens 
 * @author Kajal Rawal
 */
@Component
public class JwtUtil implements Serializable {

	private static final long serialVersionUID = -2550185165626007488L;

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
	
	// set in application.propeties 
	@Value("${jwt.secret}")
	private String secret;

	/**retrieve username from jwt token
	 * 
	 * @param token jwt token 
	 * @return username within token 
	 */
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	/**
	 * retrieve expiration date from jwt token
	 * @param token jwt token 
	 * @return expiration of jwt token 
	 */
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}
	
	/**
	 * 
	 * @param token jwtToken 
	 * @param claimsResolver function specifying desired claim 
	 * @return object of desired claim 
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}
	
	
    /**
     * Decrypt all claims using key 
     * @param token jwt
     * @return all claims 
     */
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	/**
	 * Check if token is expired 
	 * @param token jwt token 
	 * @return true if token is expired 
	 */
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	/**
	 * Generate a new jwt token given 
	 * a user's details 
	 * @param userDetails user information 
	 * @return jwt token as string 
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userDetails.getUsername());
	}

	/** while creating the token -
	1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	2. Sign the JWT using the HS512 algorithm and secret key.
	3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	   compaction of the JWT to a URL-safe string */ 
	private String doGenerateToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	/**
	 * return true if username of token matches 
	 * details and token is not expired 
	 * @param token jwtToken
	 * @param userDetails user information 
	 * @return true if user information matches and token is not expired 
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}