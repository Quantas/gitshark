package com.quantasnet.gitserver.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.repo.RepositoryService;

@Order(InitOrdering.REPO_SCAN)
@Component
public class GitRepoScanner implements Initializer {

	private static final Logger LOG = LoggerFactory.getLogger(GitRepoScanner.class);
	
	@Autowired
	private RepositoryService repoService;
	
	@Override
	public void init() throws Exception {
		LOG.info("Scanning for existing repos...");
		repoService.getRepositories().forEach(repo -> LOG.info("Repo Found: {}", repo));
	}

}
