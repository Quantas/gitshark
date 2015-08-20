package com.quantasnet.gitshark.git.protocol.hooks.pre;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
public interface GitSharkPreReceiveHook {
    void onPreReceive(ReceivePack rp, Collection<ReceiveCommand> commands, User user, GitRepository repo);
}
