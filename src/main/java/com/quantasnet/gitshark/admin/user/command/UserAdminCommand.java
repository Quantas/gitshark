package com.quantasnet.gitshark.admin.user.command;

import org.springframework.beans.factory.annotation.Autowired;

import com.quantasnet.gitshark.admin.user.UserAdminService;

public abstract class UserAdminCommand {

	@Autowired
	protected UserAdminService userAdminService;

	public abstract void doAction(String userId);
	public abstract String successMessage(String userId);
	public abstract String failureMessage(String userId);
	public abstract UserAdminCommands getCommandType();
}
