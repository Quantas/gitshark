package com.quantasnet.gitserver.user;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitserver.Constants;

@RequestMapping("/profile")
@Controller
public class ProfileController {

	private static final String PROFILE_USER = "profileUser";
	private static final String CHANGE_PASSWORD = "changePasswordForm";

	@Autowired
	private UserService userService;

	@RequestMapping
	public String profile(@AuthenticationPrincipal final User user, final Model model) {
		if (!model.containsAttribute(PROFILE_USER)) {
			final User profileUser = userService.getUserById(user.getId());
			model.addAttribute(PROFILE_USER, profileUser);
		}
		if (!model.containsAttribute(CHANGE_PASSWORD)) {
			model.addAttribute(CHANGE_PASSWORD, new ChangePasswordForm());
		}
		return "user/profile";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveProfile(@AuthenticationPrincipal final User user, @ModelAttribute(PROFILE_USER) final User profileUser, final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
		final User emailCheck = userService.getUserByEmail(profileUser.getEmail());

		if (null != emailCheck && !emailCheck.getId().equals(user.getId())) {
			bindingResult.addError(new FieldError(PROFILE_USER, "email", "Email already taken"));
		}

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + PROFILE_USER, bindingResult);
			redirectAttributes.addFlashAttribute(PROFILE_USER, profileUser);
		} else {
			userService.profileUpdate(user, profileUser);
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Profile successfully updated.");
		}

		return "redirect:/profile";
	}

	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
	public String changePassword(@AuthenticationPrincipal final User user, @Valid final ChangePasswordForm changePasswordForm, final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {

		if (!bindingResult.hasErrors()) {
			final boolean success = userService.changePassword(user, changePasswordForm);
			if (!success) {
				bindingResult.addError(new FieldError(CHANGE_PASSWORD, "newPassword", "The password change failed"));
			}
		}

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + CHANGE_PASSWORD, bindingResult);
			redirectAttributes.addFlashAttribute(CHANGE_PASSWORD, changePasswordForm);
		} else {
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Password successfully changed.");
		}

		return "redirect:/profile";
	}

}
