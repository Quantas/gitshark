package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.Constants;

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
	
	public File getOwnerRootDir(final String owner) {
		return new File(getReposRoot() + File.separatorChar + owner);
	}
	
	public File getRepoDir(final String owner, final String repoName) {
		return new File(getReposRoot() + File.separatorChar + owner, repoName);
	}
	
	private String expand(final String location) {
		return location
				.replaceAll("/", Matcher.quoteReplacement(File.separator))
				.replaceFirst("^~", Matcher.quoteReplacement(Constants.OS_USER_HOME));
	}
}
