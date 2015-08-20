package com.quantasnet.gitshark.git.exception;

/**
 * Created by andrewlandsverk on 7/13/15.
 */
public class GitSharkRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GitSharkRuntimeException(final Exception cause) {
		super(cause);
	}

	public GitSharkRuntimeException(final String message) {
		super(message);
	}
}
