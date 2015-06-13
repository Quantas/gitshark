package com.quantasnet.gitserver.git.exception;

public class RepositoryAccessDeniedException extends Exception {

	private static final long serialVersionUID = 1L;

	public RepositoryAccessDeniedException() {
		super("Access Denied");
	}

}
