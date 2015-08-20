package com.quantasnet.gitshark;

import org.springframework.http.HttpHeaders;

public abstract class Constants {
	
	public static final String DOT_GIT_SUFFIX = ".git";
	

	public static final String HEADER_USER_AGENT = HttpHeaders.USER_AGENT;
	
	
	public static final String HEAD = "HEAD";
	
	public static final String REFS_HEADS = "refs/heads/";
	
	public static final String REFS_TAGS = "refs/tags/";
	
	
	public static final String GIT_RECEIVE_PACK = "git-receive-pack";
	
	public static final String GIT_RECEIVE_PACK_ADV = "application/x-git-receive-pack-advertisement";
	
	public static final String GIT_RECEIVE_PACK_RESULT = "application/x-git-receive-pack-result";
	
	public static final String GIT_RECEIVE_PACK_REQUEST = "application/x-git-receive-pack-request";
	
	
	public static final String GIT_UPLOAD_PACK = "git-upload-pack";
	
	public static final String GIT_UPLOAD_PACK_ADV = "application/x-git-upload-pack-advertisement";
	
	public static final String GIT_UPLOAD_PACK_REQUEST = "application/x-git-upload-pack-request";
	
	public static final String GIT_UPLOAD_PACK_RESULT = "application/x-git-upload-pack-result";
	
	
	public static final String TEXT_PLAIN = "text/plain";
	
	
	public static final String OS_USER_HOME = System.getProperty("user.home");
	
	
	public static final String SUCCESS_STATUS = "successStatus";
	
	public static final String FAILURE_STATUS = "failureStatus";
	
	private Constants() {
	}

}
