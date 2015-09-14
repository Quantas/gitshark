package com.quantasnet.gitshark.admin.user.command;

import org.springframework.stereotype.Component;

@Component
class MakeAdminCommand extends UserAdminCommand {

	@Override
	public void doAction(String userId) {
		userAdminService.makeAdmin(userId);
	}

	@Override
	public String successMessage(String userId) {
		return "User " + userId + " was made an Admin.";
	}

	@Override
	public String failureMessage(String userId) {
		return "User " + userId + " was not made an Admin.";
	}

	@Override
	public UserAdminCommands getCommandType() {
		return UserAdminCommands.MAKEADMIN;
	}
}
