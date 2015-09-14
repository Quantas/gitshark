package com.quantasnet.gitshark.admin.user.command;

import org.springframework.stereotype.Component;

@Component
class RevokeAdminCommand extends UserAdminCommand {

	@Override
	public void doAction(String userId) {
		userAdminService.revokeAdmin(userId);
	}

	@Override
	public String successMessage(String userId) {
		return "User " + userId + " had Admin revoked.";
	}

	@Override
	public String failureMessage(String userId) {
		return "User " + userId + " could not have Admin revoked.";
	}

	@Override
	public UserAdminCommands getCommandType() {
		return UserAdminCommands.REVOKEADMIN;
	}
}