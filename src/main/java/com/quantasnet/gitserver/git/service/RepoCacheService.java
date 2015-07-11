package com.quantasnet.gitserver.git.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * Created by andrewlandsverk on 7/10/15.
 */
@Service
public class RepoCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(RepoCacheService.class);

    public static final String ALL_COMMITS = "allCommits";
    public static final String COMMIT = "commit";

    @Caching(evict = {
        @CacheEvict(cacheNames = { ALL_COMMITS }, allEntries = true)
    })
    public void clearCache() {
        LOG.info("Wiping caches");
    }

}
