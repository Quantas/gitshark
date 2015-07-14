package com.quantasnet.gitserver.git.exception;

/**
 * Created by andrewlandsverk on 7/13/15.
 */
public class GitServerRuntimeException extends RuntimeException {

	public GitServerRuntimeException(final Exception cause) {
		super(cause);
	}

	public GitServerRuntimeException(final String message) {
		super(message);
	}
}
