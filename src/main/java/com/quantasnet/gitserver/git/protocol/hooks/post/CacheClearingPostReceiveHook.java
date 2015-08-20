package com.quantasnet.gitserver.git.protocol.hooks.post;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.cache.EvictAllCaches;
import com.quantasnet.gitserver.git.cache.EvictRepoCache;
import com.quantasnet.gitserver.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
@Component
public class CacheClearingPostReceiveHook implements GitSharkPostReceiveHook {

	private static final Logger LOG = LoggerFactory.getLogger(CacheClearingPostReceiveHook.class);

	@EvictRepoCache
	@EvictAllCaches
	@Override
	public void onPostReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands, final User user, final GitRepository repo) {
		LOG.info("Clearing All Caches and Cache for {}", repo.getFullDisplayName());
	}
}
