package com.quantasnet.gitserver.exception;

public class RepositoryNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public RepositoryNotFoundException(final String name) {
		super("Invalid Git repository name - " + name);
	}
}
