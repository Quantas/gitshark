package com.quantasnet.gitserver.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.exception.ServerInitializerException;
import com.quantasnet.gitserver.git.repo.RepoFolderUtil;

@Order(InitOrdering.SERVER_ROOT)
@Component
public class GitServerFolderInitalizer extends FolderInitializer {

	@Autowired
	private RepoFolderUtil folderUtil;
	
	@Override
	public void init() throws ServerInitializerException {
		initializeDirectory(folderUtil.getRoot(), "Git Root");
		initializeDirectory(folderUtil.getReposRoot(), "Git Repos");
	}
	
	@Override
	public void stop() {
	}

}