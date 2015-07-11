package com.quantasnet.gitserver.git.protocol.packs;

import com.quantasnet.gitserver.git.protocol.hooks.post.GitServerPostReceiveHook;
import com.quantasnet.gitserver.git.protocol.hooks.pre.GitServerPreReceiveHook;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import java.util.Collection;
import java.util.List;

public class GitServerReceivePack extends ReceivePack implements PreReceiveHook, PostReceiveHook {

	private final GitRepository repo;
	private final User user;

	private final List<GitServerPreReceiveHook> preReceiveHooks;
	private final List<GitServerPostReceiveHook> postReceiveHooks;
	
	public GitServerReceivePack(final Repository into, final GitRepository repo, final User user,
			final List<GitServerPreReceiveHook> preReceiveHooks, final List<GitServerPostReceiveHook> postReceiveHooks) {
		super(into);
		this.repo = repo;
		this.user = user;
		this.preReceiveHooks = preReceiveHooks;
		this.postReceiveHooks = postReceiveHooks;

		setPreReceiveHook(this);
		setPostReceiveHook(this);
	}
	
	@Override
	public void onPreReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands) {
		preReceiveHooks.forEach(receiveHook -> receiveHook.onPreReceive(rp, commands, user, repo));
	}

	@Override
	public void onPostReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands) {
		postReceiveHooks.forEach(receiveHook -> receiveHook.onPostReceive(rp, commands, user, repo));
	}
}
