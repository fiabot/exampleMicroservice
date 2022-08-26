package com.usbank.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;


import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Set up security configurations 
 * @author fmshyne and Kajal Rawal
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	/**
	 * Pass user detail service and password encoder 
	 * into authentication manager
	 * 
	 *  This is done automatically by spring 
	 * @param auth authentication manager builder 
	 * @throws Exception
	 */
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials
		// Use BCryptPasswordEncoder
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
	}
	
	/**
	 * Turn off encoding for security 
	 * 
	 * This presents a security that should be changed 
	 * to more secure encoding for production code 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
	
	/**
	 * Enable authentication manager for 
	 * autowiring 
	 * 
	 * This is done automatically by spring 
	 * @param authConfig configuration settings, see above 
	 * @return authentication manager 
	 * @throws Exception
	 */
	@Bean
	public AuthenticationManager authenticationManager(
	        AuthenticationConfiguration authConfig) throws Exception {
	    return authConfig.getAuthenticationManager();
	}
	
	
	/**
	 * Configure http security settings: 
	 * 		1. Make session management stateless (using jwtTokens instead) 
	 * 		2. disable csrf 
	 * 		3. set entry point 
	 * 		4. make any admin/* requests only accessible to admins 
	 * 		5. make any user/* requests only accesssible to users 
	 * 		6. apply request fileter to all requests 
	 * 
	 * Note: GraphQL doesn't have unique http requests, so 
	 * security is done at the method level. See GraphQLController 
	 * 
	 * This is done automatically by spring 
	 */
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.cors().and().csrf().disable()
			.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() // jwt tokens make session stateless 
			.authorizeRequests().antMatchers("/admin/**").hasAuthority("admin") // for RESt requests 
			.antMatchers("/user/**").hasAuthority("user"); // for REST requests 
		// Add a filter to validate the tokens with every request
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build(); 
	}
	
}