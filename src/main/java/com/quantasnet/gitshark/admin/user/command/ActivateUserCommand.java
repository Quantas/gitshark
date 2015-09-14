package com.quantasnet.gitshark.admin.user.command;

import org.springframework.stereotype.Component;

@Component
class ActivateUserCommand extends UserAdminCommand {

	@Override
	public void doAction(String userId) {
		userAdminService.activateUser(userId);
	}

	@Override
	public String successMessage(String userId) {
		return "User " + userId + " was successfully activated.";
	}

	@Override
	public String failureMessage(String userId) {
		return "User " + userId + " could not be activated.";
	}

	@Override
	public UserAdminCommands getCommandType() {
		return UserAdminCommands.ACTIVATE;
	}
}