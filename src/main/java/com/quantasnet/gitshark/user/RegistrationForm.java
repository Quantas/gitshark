package com.quantasnet.gitshark.user;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class RegistrationForm {

    @NotEmpty(message = "Username is required.")
    @Size(min = 4, max = 255, message = "Username must be between at least 4 characters.")
    private String userName;

    @NotEmpty(message = "Password is required.")
    @Pattern(regexp = User.PASSWORD_REGEX, message = "Password must be at least 8 characters and contain a number.")
    private String password;

	@NotEmpty(message = "Email is required.")
	@Email(message = "Email must be well formatted.")
	private String email;

    @NotEmpty(message = "First name is required.")
    @Size(min = 2, max = 30, message = "First name must be at least 2 characters.")
    private String firstName;

    @NotEmpty(message = "Last name is required.")
    @Size(min = 2, max = 30, message = "Last name must be at least 2 characters.")
    private String lastName;


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
