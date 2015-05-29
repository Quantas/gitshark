package com.quantasnet.gitserver.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.repo.RepoFolderUtil;

@Order(InitOrdering.SERVER_ROOT)
@Component
public class GitServerFolderInitalizer extends FolderInitializer {

	@Autowired
	private RepoFolderUtil folderUtil;
	
	@Override
	public void init() throws Exception {
		initializeDirectory(folderUtil.getRoot(), "Git Root");
		initializeDirectory(folderUtil.getReposRoot(), "Git Repos");
		initializeDirectory(folderUtil.getUserReposRoot(), "Git User Repos");
		initializeDirectory(folderUtil.getProjectReposRoot(), "Git Project Repos");
	}

}