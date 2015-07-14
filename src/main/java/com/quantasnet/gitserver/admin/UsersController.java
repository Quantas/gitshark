package com.quantasnet.gitserver.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.user.UserService;

/**
 * Created by andrewlandsverk on 7/13/15.
 */
@Controller
@RequestMapping("/admin/users")
public class UsersController {

	@Autowired
	private UserService userService;

	@RequestMapping(method = RequestMethod.GET)
	public String users(final Model model) {
		model.addAttribute("users", userService.getAll());
		return "admin/users";
	}
}
