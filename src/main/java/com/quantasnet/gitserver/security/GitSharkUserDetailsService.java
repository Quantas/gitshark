package com.quantasnet.gitserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.quantasnet.gitserver.user.UserService;

public class GitSharkUserDetailsService implements UserDetailsService {

	@Autowired
	private UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		return userService.getUserByUsername(username);
	}

}
