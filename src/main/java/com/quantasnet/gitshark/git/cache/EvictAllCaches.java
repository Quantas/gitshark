package com.quantasnet.gitshark.git.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.CacheEvict;

/**
 * Evict the all caches.
 *
 * Created by andrewlandsverk on 8/5/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CacheEvict(cacheNames = { RepoCacheConstants.ALL_COMMITS, RepoCacheConstants.ALL_READMES }, allEntries = true)
public @interface EvictAllCaches {
}
