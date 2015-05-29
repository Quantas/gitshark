package com.quantasnet.gitserver.util;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitServerFolderUtil {

	@Value("${git.dir.root}")
	private String gitRoot;
	
	@Value("${git.dir.repos}")
	private String repos;
	
	public String getRoot() {
		return gitRoot;
	}
	
	public String getRepos() {
		return getRoot() + File.separatorChar + repos;
	}
}
