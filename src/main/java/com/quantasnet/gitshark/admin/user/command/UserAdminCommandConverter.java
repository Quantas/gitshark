package com.quantasnet.gitshark.admin.user.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserAdminCommandConverter implements Converter<String, UserAdminCommand> {

	@Autowired
	private UserAdminCommandsHolder holder;

	@Override
	public UserAdminCommand convert(final String source) {
		return holder.getCommandForType(source);
	}

}
