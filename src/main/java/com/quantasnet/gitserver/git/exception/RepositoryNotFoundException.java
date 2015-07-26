package com.quantasnet.gitserver.git.exception;

public class RepositoryNotFoundException extends GitServerException {

	private static final long serialVersionUID = 1L;

	public RepositoryNotFoundException(final String name) {
		super("Invalid Git repository name - " + name);
	}
}
