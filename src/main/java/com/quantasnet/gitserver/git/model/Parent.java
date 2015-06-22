package com.quantasnet.gitserver.git.model;

import com.quantasnet.gitserver.git.repo.GitRepository;

public class Parent {

	private final String id;
	private final String url;
	
	public Parent(final String id, final GitRepository repo) {
		this.id = id;
		this.url = buildUrl(repo);
	}
	
	private String buildUrl(final GitRepository repo) {
		return repo.getInterfaceBaseUrl() + "/commit/" +  id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUrl() {
		return url;
	}
}
