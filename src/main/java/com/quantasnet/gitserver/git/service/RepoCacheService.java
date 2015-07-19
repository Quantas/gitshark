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

    public static final String README = "readme";
    public static final String HAS_COMMITS = "hasCommits";
    public static final String REPO_SIZE = "repoSize";

    @CacheEvict(cacheNames = { ALL_COMMITS, ALL_READMES }, allEntries = true)
    public void clearCache() {
        LOG.info("Wiping caches");
    }

    @CacheEvict(cacheNames = { README, HAS_COMMITS, REPO_SIZE }, key = "#repo.fullDisplayName")
    public void clearCacheForRepo(final GitRepository repo) {
        LOG.info("Wiping caches for repo {}", repo.getFullDisplayName());
    }
}
