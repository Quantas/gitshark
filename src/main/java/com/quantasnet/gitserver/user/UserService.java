package com.quantasnet.gitserver.user;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.quantasnet.gitserver.register.RegistrationForm;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
    public User getUserByUsername(final String username) {
        return userRepository.getUserByUserName(username);
    }

    public User getUserByEmail(final String email) {
        return userRepository.getUserByEmail(email);
    }
	
    public long count() {
    	return userRepository.count();
    }
    
    public User registerNewUser(final RegistrationForm form) {
    	final User user = new User();
    	user.setUserName(form.getUserName());
    	user.setFirstName(form.getFirstName());
    	user.setLastName(form.getLastName());
    	user.setEmail(form.getEmail());
    	user.setPassword(passwordEncoder.encode(form.getPassword()));
    	user.setActive(true);
    	user.setImageUrl("//www.gravatar.com/avatar/" + DigestUtils.md5Hex(user.getEmail().trim().toLowerCase()) + "?d=identicon&rating=g");
    	user.setRoles(Sets.newHashSet(roleService.findUserRole()));
    	return userRepository.saveAndFlush(user);
    }
    
    public User updateUser(final User user) {
    	return userRepository.saveAndFlush(user);
    }
    
}
