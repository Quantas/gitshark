package com.quantasnet.gitshark.git.exception;

public class RepositoryAccessDeniedException extends Exception {

	private static final long serialVersionUID = 1L;

	public RepositoryAccessDeniedException() {
		super("Access Denied");
	}

}
