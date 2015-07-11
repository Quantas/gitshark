package com.quantasnet.gitserver.git.protocol.packs;

import com.quantasnet.gitserver.git.protocol.hooks.post.GitServerPostReceiveHook;
import com.quantasnet.gitserver.git.protocol.hooks.pre.GitServerPreReceiveHook;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
@Component
public class GitServerReceivePackFactory {

    @Autowired
    private List<GitServerPreReceiveHook> preReceiveHooks;

    @Autowired
    private List<GitServerPostReceiveHook> postReceiveHooks;

    public GitServerReceivePack createReceivePack(final Repository into, final GitRepository repo, final User user) {
        return new GitServerReceivePack(into, repo, user, preReceiveHooks, postReceiveHooks);
    }

}
