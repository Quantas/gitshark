package com.quantasnet.gitshark.git.exception;

public class CommitNotFoundException extends GitSharkException {

	private static final long serialVersionUID = 1L;

	public CommitNotFoundException(final String commitID) {
		this(commitID, null);
	}
	
	public CommitNotFoundException(final String commitID, final Exception cause) {
		super("Commit not found - " + commitID, cause);
	}
}
