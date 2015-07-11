package com.quantasnet.gitserver.git.protocol.hooks.post;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import java.util.Collection;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
public interface GitServerPostReceiveHook {
    void onPostReceive(ReceivePack rp, Collection<ReceiveCommand> commands, User user, GitRepository repo);
}
