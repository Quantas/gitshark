package com.quantasnet.gitserver.git.exception;

public class CommitNotFoundException extends GitServerException {

	private static final long serialVersionUID = 1L;

	public CommitNotFoundException(final String commitID) {
		this(commitID, null);
	}
	
	public CommitNotFoundException(final String commitID, final Exception cause) {
		super("Commit not found - " + commitID, cause);
	}
}
