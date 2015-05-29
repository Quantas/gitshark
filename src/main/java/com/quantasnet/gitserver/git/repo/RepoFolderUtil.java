package com.quantasnet.gitserver.git.repo;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RepoFolderUtil {

	@Value("${git.dir.root}")
	private String gitRoot;
	
	@Value("${git.dir.repos}")
	private String repos;
	
	@Value("${git.dir.users}")
	private String userRepos;
	
	@Value("${git.dir.projects}")
	private String projectRepos;
	
	public String getRoot() {
		return expand(gitRoot);
	}
	
	public String getReposRoot() {
		return getRoot() + File.separatorChar + repos;
	}
	
	public String getUserReposName() {
		return userRepos;
	}
	
	public String getUserReposRoot() {
		return getReposRoot() + File.separatorChar + userRepos;
	}
	
	public String getProjectReposName() {
		return projectRepos;
	}
	
	public String getProjectReposRoot() {
		return getReposRoot() + File.separatorChar + projectRepos;
	}
	
	private String expand(final String location) {
		return location.replaceFirst("^~",System.getProperty("user.home"));
	}
}
