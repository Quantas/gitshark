package com.quantasnet.gitserver.git.protocol.packs;

import java.util.Collection;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;

import com.quantasnet.gitserver.git.repo.GitRepository;

public class GitServerReceivePack extends ReceivePack implements PreReceiveHook, PostReceiveHook {

	private static final Logger LOG = LoggerFactory.getLogger("GitServerReceive");
	
	private final GitRepository repo;
	private final User user;
	
	public GitServerReceivePack(final Repository into, final GitRepository repo, final User user) {
		super(into);
		this.repo = repo;
		this.user = user;
		setPreReceiveHook(this);
		setPostReceiveHook(this);
	}
	
	@Override
	public void onPreReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands) {
		final String username = user == null ? "anon" : user.getUsername();
		
		commands.forEach(command -> {
			switch(command.getType()) {
			case CREATE:
				LOG.info("{} - CREATE {} by {}", repo.getFullDisplayName(), command.getNewId().getName(), username);
				break;
			case DELETE:
				LOG.info("{} - DELETE {} by {}", repo.getFullDisplayName(), command.getOldId().getName(), username);
				break;
			case UPDATE:
				LOG.info("{} - UPDATE Old={}, New={} by {}", repo.getFullDisplayName(), command.getOldId().getName(), command.getNewId().getName(), username);
				break;
			case UPDATE_NONFASTFORWARD:
				LOG.info("{} - UPDATE NON FF Old={}, New={} by {}", repo.getFullDisplayName(), command.getOldId().getName(), command.getNewId().getName(), username);
				break;
			default:
				break;
			}
		});
	}

	@Override
	public void onPostReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands) {
		
	}
}
