package com.quantasnet.gitserver.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GitServerUserDetailsService implements UserDetailsService {

	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		// TODO obviously we need to get this from the DB
		return new User(username, passwordEncoder.encode(username), true, true, true, true, Arrays.asList(new SimpleGrantedAuthority("USER")));
	}

}
