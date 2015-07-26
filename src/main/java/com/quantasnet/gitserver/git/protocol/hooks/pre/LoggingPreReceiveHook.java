package com.quantasnet.gitserver.git.protocol.hooks.pre;

import java.util.Collection;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;

/**
 * Created by andrewlandsverk on 7/11/15.
 */
@Component
public class LoggingPreReceiveHook implements GitServerPreReceiveHook {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingPreReceiveHook.class);

    @Override
    public void onPreReceive(final ReceivePack rp, final Collection<ReceiveCommand> commands, final User user, final GitRepository repo) {
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
