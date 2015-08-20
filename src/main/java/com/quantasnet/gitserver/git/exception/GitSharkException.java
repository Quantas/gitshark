package com.quantasnet.gitserver.git.exception;

/**
 * Spring MVC is configured to cause this exception to render 404.
 * 
 * @author Andrew
 */
public class GitSharkException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitSharkException() {
	}
	
	public GitSharkException(final String message) {
		super(message);
	}
	
	public GitSharkException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public GitSharkException(final Throwable cause) {
		super(cause);
	}
	
}
