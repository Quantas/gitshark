package com.quantasnet.gitserver.git.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Created by andrewlandsverk on 7/10/15.
 */
@Service
public class RepoCacheService {

	private static final Logger LOG = LoggerFactory.getLogger(RepoCacheService.class);

	public static final String ALL_COMMITS = "allCommits";
	public static final String ALL_READMES = "allReadmes";

	public static final String COMMIT = "commit";

	public static final String HAS_COMMITS = "hasCommits";
	public static final String BRANCHES = "branches";
	public static final String TAGS = "tags";
	public static final String COMMIT_COUNT = "commitCount";
	public static final String REPO_SIZE = "repoSize";

	@CacheEvict(cacheNames = { ALL_COMMITS, ALL_READMES }, allEntries = true)
	public void clearCache() {
		LOG.info("Wiping caches");
	}

	@CacheEvict(cacheNames = { HAS_COMMITS, COMMIT_COUNT, REPO_SIZE, BRANCHES, TAGS }, key = "#repo.fullDisplayName")
	public void clearCacheForRepo(final GitRepository repo) {
		LOG.info("Wiping caches for repo {}", repo.getFullDisplayName());
	}
}
