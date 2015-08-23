package com.quantasnet.gitshark.git.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.CacheEvict;

/**
 * Evict the GitRepository cache.
 *
 * Created by andrewlandsverk on 8/5/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CacheEvict(cacheNames = { RepoCacheConstants.HAS_COMMITS, RepoCacheConstants.COMMIT_COUNT, RepoCacheConstants.BRANCHES, RepoCacheConstants.TAGS, RepoCacheConstants.REFLOG }, key = "#repo.fullDisplayName")
public @interface EvictRepoCache {
}
