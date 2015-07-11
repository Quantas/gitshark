package com.quantasnet.gitserver.git.protocol.hooks.pre;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
public interface GitServerPreReceiveHook {
    void onPreReceive(ReceivePack rp, Collection<ReceiveCommand> commands, User user, GitRepository repo);
}
