package com.quantasnet.gitserver.git.protocol.packs;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.Utils;
import com.quantasnet.gitserver.git.protocol.hooks.post.GitSharkPostReceiveHook;
import com.quantasnet.gitserver.git.protocol.hooks.pre.GitSharkPreReceiveHook;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
@Component
public class GitSharkReceivePackFactory {

	@Autowired(required = false)
	private List<GitSharkPreReceiveHook> preReceiveHooks;

	@Autowired(required = false)
	private List<GitSharkPostReceiveHook> postReceiveHooks;

	public GitSharkReceivePack createReceivePack(final Repository into, final GitRepository repo, final User user) {
		return new GitSharkReceivePack(into, repo, user, Utils.safeguard(preReceiveHooks), Utils.safeguard(postReceiveHooks));
	}
}
