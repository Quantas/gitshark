package com.quantasnet.gitserver.git.exception;

/**
 * Spring MVC is configured to cause this exception to render 404.
 * 
 * @author Andrew
 */
public class GitServerException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitServerException() {
	}
	
	public GitServerException(final String message) {
		super(message);
	}
	
	public GitServerException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public GitServerException(final Throwable cause) {
		super(cause);
	}
	
}
