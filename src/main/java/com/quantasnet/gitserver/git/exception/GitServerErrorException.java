package com.quantasnet.gitserver.git.exception;

/**
 * Spring MVC is configured to cause this exception to render 503.
 * 
 * @author Andrew
 */
public class GitServerErrorException extends GitServerException {
	
	private static final long serialVersionUID = 1L;

	public GitServerErrorException() {
		super();
	}

	public GitServerErrorException(final Throwable cause) {
		super(cause);
	}

	public GitServerErrorException(final String message) {
		super(message);
	}

	public GitServerErrorException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
