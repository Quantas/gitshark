package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.util.regex.Matcher;

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
		return location
				.replaceAll("/", Matcher.quoteReplacement(File.separator))
				.replaceFirst("^~", Matcher.quoteReplacement(System.getProperty("user.home")));
	}
}
