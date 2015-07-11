package com.quantasnet.gitserver.git.protocol.hooks.post;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.RepoCacheService;
import com.quantasnet.gitserver.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
@Component
public class CacheClearingPostReceiveHook implements GitServerPostReceiveHook {

    @Autowired
    private RepoCacheService repoCacheService;

    @Override
    public void onPostReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands, final User user, final GitRepository repo) {
        repoCacheService.clearCache();
    }
}
