package com.quantasnet.gitserver.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.util.GitServerFolderUtil;

@Order(InitOrdering.SERVER_ROOT)
@Component
public class GitServerFolderInitalizer extends FolderInitializer {

	@Autowired
	private GitServerFolderUtil folderUtil;
	
	@Override
	public void init() throws Exception {
		initializeDirectory(folderUtil.getRoot(), "Git Root");
		initializeDirectory(folderUtil.getRepos(), "Git Repos");
	}

}