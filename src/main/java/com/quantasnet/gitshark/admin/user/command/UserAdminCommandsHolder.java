package com.quantasnet.gitshark.admin.user.command;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAdminCommandsHolder {

	@Autowired
	private List<UserAdminCommand> userAdminCommands;

	private Map<UserAdminCommands, UserAdminCommand> userAdminCommandMap;

	@PostConstruct
	public void postConstruct() {
		userAdminCommandMap = new EnumMap<>(UserAdminCommands.class);

		for (final UserAdminCommand command : userAdminCommands) {
			userAdminCommandMap.put(command.getCommandType(), command);
		}
	}

	public UserAdminCommand getCommandForType(final String type) {
		final UserAdminCommands command = UserAdminCommands.convertFromText(type);
		if (null == command) {
			throw new IllegalUserAdminCommandException();
		}
		return userAdminCommandMap.get(command);
	}
}