package com.quantasnet.gitshark.git.exception;

public class ServerInitializerException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ServerInitializerException(final String message) {
		super(message);
	}
	
	public ServerInitializerException(final Throwable cause) {
		super(cause);
	}
	
}
