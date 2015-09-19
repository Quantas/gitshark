package com.quantasnet.gitshark.user;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegistrationController {

	private static final String REGISTRATION_FORM = "registrationForm";
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String registerForm(final Model model) {
		if (!model.containsAttribute(REGISTRATION_FORM)) {
			model.addAttribute(REGISTRATION_FORM, new RegistrationForm());
		}
		
		return "register";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String saveForm(@Valid final RegistrationForm form, final BindingResult result, @RequestParam(defaultValue = "") final String passwordAgain, final RedirectAttributes redirectAttributes) {
		
		final User userNameCheck = userService.getUserByUsername(form.getUserName());

		if (null != userNameCheck) {
			result.addError(new FieldError(REGISTRATION_FORM, "userName", "Username already taken"));
		}
		
		final User emailCheck = userService.getUserByEmail(form.getEmail());
		
		if (null != emailCheck) {
			result.addError(new FieldError(REGISTRATION_FORM, "email", " Email already taken"));
		}

		if (!passwordAgain.equals(form.getPassword())) {
			result.addError(new FieldError(REGISTRATION_FORM, "password", "Confirm password must match"));
		}
		
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute(REGISTRATION_FORM, form);
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + REGISTRATION_FORM, result);
			return "redirect:/register";
		}
		
		final User newUser = userService.registerNewUser(form);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(newUser, newUser.getPassword(), newUser.getAuthorities()));
		return "redirect:/repo";
	}
}
