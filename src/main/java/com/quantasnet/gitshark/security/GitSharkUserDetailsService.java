package com.quantasnet.gitshark.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.quantasnet.gitshark.user.UserService;

public class GitSharkUserDetailsService implements UserDetailsService {

	@Autowired
	private UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(final String username) {
		return userService.getUserByUsername(username);
	}

}
