package com.quantasnet.gitshark.git.protocol.packs;

import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import com.quantasnet.gitshark.git.protocol.hooks.post.GitSharkPostReceiveHook;
import com.quantasnet.gitshark.git.protocol.hooks.pre.GitSharkPreReceiveHook;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.user.User;

public class GitSharkReceivePack extends ReceivePack implements PreReceiveHook, PostReceiveHook {

	private final GitRepository repo;
	private final User user;

	private final List<GitSharkPreReceiveHook> preReceiveHooks;
	private final List<GitSharkPostReceiveHook> postReceiveHooks;
	
	public GitSharkReceivePack(final Repository into, final GitRepository repo, final User user,
			final List<GitSharkPreReceiveHook> preReceiveHooks, final List<GitSharkPostReceiveHook> postReceiveHooks) {
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
