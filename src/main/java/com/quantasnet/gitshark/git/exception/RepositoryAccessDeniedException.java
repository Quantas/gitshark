package com.quantasnet.gitshark.git.exception;

public class RepositoryAccessDeniedException extends GitSharkException {

	private static final long serialVersionUID = 1L;

	public RepositoryAccessDeniedException() {
		super("Access Denied");
	}

}
