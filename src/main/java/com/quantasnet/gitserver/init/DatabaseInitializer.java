package com.quantasnet.gitserver.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.exception.ServerInitializerException;
import com.quantasnet.gitserver.user.RegistrationForm;
import com.quantasnet.gitserver.user.Role;
import com.quantasnet.gitserver.user.RoleService;
import com.quantasnet.gitserver.user.User;
import com.quantasnet.gitserver.user.UserService;

@Order(InitOrdering.DATABASE)
@Component
public class DatabaseInitializer implements Initializer {

	@Autowired
	private RoleService roleService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void init() throws ServerInitializerException {
		
		if (roleService.count() == 0L) {
			roleService.save(Role.USER);
			roleService.save(Role.ADMIN);
		}
		
		if (userService.count() == 0L) {
			final RegistrationForm user = new RegistrationForm();
			user.setUserName("user");
			user.setFirstName("Andrew");
			user.setLastName("Landsverk");
			user.setEmail("dewdew@gmail.com");
			user.setPassword("user");
			userService.registerNewUser(user);
			
			final RegistrationForm admin = new RegistrationForm();
			admin.setUserName("admin");
			admin.setFirstName("Admin");
			admin.setLastName("Administrator");
			admin.setEmail("webmaster@quantasnet.com");
			admin.setPassword("admin");
			final User adminUser = userService.registerNewUser(admin);
			
			adminUser.getRoles().add(roleService.findAdminRole());
			userService.updateUser(adminUser);
		}
	}

	@Override
	public void stop() {
	}

}
