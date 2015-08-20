package com.quantasnet.gitserver.git.protocol.hooks.post;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
public interface GitSharkPostReceiveHook {
    void onPostReceive(ReceivePack rp, Collection<ReceiveCommand> commands, User user, GitRepository repo);
}
