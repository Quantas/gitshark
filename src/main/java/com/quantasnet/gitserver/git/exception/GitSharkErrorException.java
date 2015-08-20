package com.quantasnet.gitserver.git.exception;

/**
 * Spring MVC is configured to cause this exception to render 503.
 * 
 * @author Andrew
 */
public class GitSharkErrorException extends GitSharkException {
	
	private static final long serialVersionUID = 1L;

	public GitSharkErrorException() {
		super();
	}

	public GitSharkErrorException(final Throwable cause) {
		super(cause);
	}

	public GitSharkErrorException(final String message) {
		super(message);
	}

	public GitSharkErrorException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
