package com.quantasnet.gitshark.git.protocol.hooks.post;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
@Component
public class LoggingPostReceiveHook implements GitSharkPostReceiveHook {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingPostReceiveHook.class);

    @Override
    public void onPostReceive(ReceivePack rp, Collection<ReceiveCommand> commands, User user, GitRepository repo) {
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
}
