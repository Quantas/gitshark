package com.quantasnet.gitshark.git.protocol.hooks.post;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceiveCommand.Result;
import org.eclipse.jgit.transport.ReceivePack;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitshark.git.dfs.GitSharkDfsRefLog;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.user.User;

@Component
public class RefLogWritingPostReceiveHook implements GitSharkPostReceiveHook {

	@Autowired
	private GitSharkDfsService dfsService;

	@Override
	public void onPostReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands, final User user, final GitRepository repo) {
		final String username = user == null ? "anon" : user.getUsername();
		
		commands.forEach(command -> {
			if (Result.OK == command.getResult()) {
				final GitSharkDfsRefLog refLog = dfsService.createEmptyRefLog();
	
				refLog.setRepoId(repo.getId());
				refLog.setBranch(command.getRefName());
				refLog.setUserName(username);
				refLog.setUserEmail(user.getEmail());
				refLog.setUserDisplayName(user.getFirstName() + ' ' + user.getLastName());
				refLog.setTime(DateTime.now());

				refLog.setType(command.getType().name());

				refLog.setOldId(command.getOldId().name());
				refLog.setNewId(command.getNewId().name());
				
				dfsService.saveRefLog(refLog);
			}
		});
	}
}
