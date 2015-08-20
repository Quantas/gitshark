package com.quantasnet.gitshark.git.exception;

public class RepositoryNotFoundException extends GitSharkException {

	private static final long serialVersionUID = 1L;

	public RepositoryNotFoundException(final String name) {
		super("Invalid Git repository name - " + name);
	}
}
