package com.quantasnet.gitserver.user;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by andrewlandsverk on 8/15/15.
 */
public class ChangePasswordForm {

	@NotEmpty(message = "Current password is required.")
	private String currentPassword;

	@NotEmpty(message = "New password is required.")
	@Size(min = 8, max = 255, message = "Password must be at least 8 characters.")
	@Pattern(regexp = User.PASSWORD_REGEX, message = "Password must be at least 8 characters and contain a number.")
	private String newPassword;

	@NotEmpty(message = "New password again is required.")
	private String newPasswordAgain;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPasswordAgain() {
		return newPasswordAgain;
	}

	public void setNewPasswordAgain(String newPasswordAgain) {
		this.newPasswordAgain = newPasswordAgain;
	}

}
