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
	
	public String getRoot() {
		return expand(gitRoot);
	}
	
	public String getReposRoot() {
		return getRoot() + File.separatorChar + repos;
	}
	
	public File getRepoDir(final String owner) {
		return new File(getReposRoot() + File.separatorChar + owner);
	}
	
	private String expand(final String location) {
		return location.replaceFirst("^~",System.getProperty("user.home"));
	}
}
