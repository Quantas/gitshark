package com.quantasnet.gitshark.admin.user.command;

import org.springframework.stereotype.Component;

@Component
class DeactivateUserCommand extends UserAdminCommand {

	@Override
	public void doAction(String userId) {
		userAdminService.deactivateUser(userId);
	}

	@Override
	public String successMessage(String userId) {
		return "User " + userId + " was successfully deactivated.";
	}

	@Override
	public String failureMessage(String userId) {
		return "User " + userId + " could not be deactivated.";
	}

	@Override
	public UserAdminCommands getCommandType() {
		return UserAdminCommands.DEACTIVATE;
	}
}
