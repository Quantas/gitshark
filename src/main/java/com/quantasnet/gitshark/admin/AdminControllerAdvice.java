package com.quantasnet.gitshark.admin;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Created by andrewlandsverk on 7/13/15.
 */
@ControllerAdvice(basePackages = "com.quantasnet.gitshark.admin")
public class AdminControllerAdvice {

	@ModelAttribute("admin")
	public Boolean admin() {
		return Boolean.TRUE;
	}
}
