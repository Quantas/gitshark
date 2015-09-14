package com.quantasnet.gitshark.admin.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.admin.user.command.UserAdminCommand;

@Controller
@RequestMapping("/admin/users")
public class UsersController {

	private static final Logger LOG = LoggerFactory.getLogger(UsersController.class);

	@Autowired
	private UserAdminService userAdminService;

	@RequestMapping(method = RequestMethod.GET)
	public String users(final Model model) {
		model.addAttribute("users", userAdminService.getAllUsers());
		return "admin/users";
	}

	@RequestMapping(value = "/{userAdminCommand}/{userId}", method = RequestMethod.GET)
	public String doUserAdminCommand(@PathVariable final UserAdminCommand userAdminCommand, @PathVariable final String userId,
									 final RedirectAttributes redirectAttributes) {
		try {
			userAdminCommand.doAction(userId);
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, userAdminCommand.successMessage(userId));
		} catch (final DataAccessException dae) {
			final String failMessage = userAdminCommand.failureMessage(userId);
			redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, failMessage);
			LOG.error(failMessage, dae);
		}

		return "redirect:/admin/users";
	}
}
