package com.quantasnet.gitshark.admin.user.command;

import org.springframework.stereotype.Component;

@Component
class DeleteUserCommand extends UserAdminCommand {

	@Override
	public void doAction(final String userId) {
		userAdminService.deleteUser(userId);
	}

	@Override
	public String successMessage(final String userId) {
		return "User " + userId + " was successfully deleted.";
	}

	@Override
	public String failureMessage(final String userId) {
		return "User " + userId + " could not be deleted.";
	}

	@Override
	public UserAdminCommands getCommandType() {
		return UserAdminCommands.DELETE;
	}
}