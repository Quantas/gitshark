package com.quantasnet.gitserver;

import org.springframework.http.HttpHeaders;

public abstract class Constants {
	
	public static final String DOT_GIT_SUFFIX = ".git";
	

	public static final String HEADER_USER_AGENT = HttpHeaders.USER_AGENT;
	
	
	public static final String HEAD = "HEAD";
	
	
	public static final String GIT_RECEIVE_PACK = "git-receive-pack";
	
	public static final String GIT_RECEIVE_PACK_RESULT = "application/x-git-receive-pack-result";
	
	public static final String GIT_UPLOAD_PACK = "git-upload-pack";
	
	public static final String GIT_UPLOAD_PACK_RESULT = "application/x-git-upload-pack-result";
	
	
	public static final String MIME_TEXT_PLAIN = "text/plain";
	
	
	public static final String OS_USER_HOME = System.getProperty("user.home");
	
	private Constants() {
	}

}