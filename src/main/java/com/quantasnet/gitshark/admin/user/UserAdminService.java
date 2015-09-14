package com.quantasnet.gitshark.admin.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quantasnet.gitshark.user.Role;
import com.quantasnet.gitshark.user.RoleService;
import com.quantasnet.gitshark.user.User;
import com.quantasnet.gitshark.user.UserRepository;

@Transactional
@Service
public class UserAdminService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleService roleService;

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public void deleteUser(final String id) {
		final User user = userRepository.findOne(id);
		userRepository.delete(user);
	}

	public void deactivateUser(final String id) {
		final User user = userRepository.findOne(id);
		user.setActive(false);
		userRepository.save(user);
	}

	public void activateUser(final String id) {
		final User user = userRepository.findOne(id);
		user.setActive(true);
		userRepository.save(user);
	}

	public void makeAdmin(final String id) {
		final User user = userRepository.findOne(id);
		user.getRoles().add(roleService.findAdminRole());
		userRepository.save(user);
	}

	public void revokeAdmin(final String id) {
		final User user = userRepository.findOne(id);
		Role toRemove = null;
		for (final Role role : user.getRoles()) {
			if (role.getRoleName().equals(Role.ADMIN)) {
				toRemove = role;
				break;
			}
		}

		user.getRoles().remove(toRemove);
		userRepository.save(user);
	}
}

