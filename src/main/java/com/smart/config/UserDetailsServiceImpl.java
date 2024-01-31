package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.entities.User;
import com.smart.repository.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService {
	// This class is implementing the interface that spring security provides. Here
	// we are matching the username that the user provides while logging in to the
	// username in the database.

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.getUserByEmail(username);
		if (user == null) {
			throw new UsernameNotFoundException("Could not find user");
		}

		UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);

		return userDetailsImpl;
	}

}