package com.quantasnet.gitshark.user;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

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

	public User getUserById(String id) {
		return userRepository.findOne(id);
	}

	public List<User> getAll() {
		return userRepository.findAll();
	}

	public User registerNewUser(final RegistrationForm form) {
		final User user = new User();
		user.setUserName(form.getUserName());
		user.setFirstName(form.getFirstName());
		user.setLastName(form.getLastName());
		user.setEmail(form.getEmail());
		user.setPassword(passwordEncoder.encode(form.getPassword()));
		user.setActive(true);
		user.setImageUrl(generateGravatarUrl(user.getEmail()));
		user.setRoles(Sets.newHashSet(roleService.findUserRole()));
		return userRepository.save(user);
	}

	public User updateUser(final User user) {
		return userRepository.save(user);
	}

	public User profileUpdate(final User authUser, final User profileUser) {
		final User dbUser = userRepository.findOne(authUser.getId());
		dbUser.setFirstName(profileUser.getFirstName());
		dbUser.setLastName(profileUser.getLastName());
		dbUser.setEmail(profileUser.getEmail());
		dbUser.setImageUrl(generateGravatarUrl(dbUser.getEmail()));

		final User newUser = userRepository.save(dbUser);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(newUser, newUser.getPassword(), newUser.getAuthorities()));
		return newUser;
	}

	public boolean changePassword(final User user, final ChangePasswordForm changePasswordForm) {
		final User dbUser = userRepository.getUserByUserName(user.getUserName());

		if (passwordEncoder.matches(changePasswordForm.getCurrentPassword(), dbUser.getPassword()) &&
				changePasswordForm.getNewPassword().equals(changePasswordForm.getNewPasswordAgain())) {
			user.setPassword(passwordEncoder.encode(changePasswordForm.getNewPassword()));
			userRepository.save(user);
			return true;
		}

		return false;
	}

	private String generateGravatarUrl(final String email) {
		return "//www.gravatar.com/avatar/" + DigestUtils.md5Hex(email.trim().toLowerCase()) + "?d=identicon&rating=g";
	}

}
