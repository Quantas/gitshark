package com.quantasnet.gitserver.init;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerRuntimeException;
import com.quantasnet.gitserver.git.exception.ServerInitializerException;

@Component
public class ServerInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(ServerInitializer.class);
	
	private static boolean alreadyInit = false;
	
	@Autowired
	private List<Initializer> initializers;
	
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		if (alreadyInit) {
			final String message = "SERVER ALREADY INITALIZED...";
			LOG.error(message);
			throw new GitServerRuntimeException(message);
		} else {
			try {
				for (final Initializer init : initializers) {
					LOG.info("Initializing - {}", init.getClass().getSimpleName());
					init.init();
				}
				alreadyInit = true;
			} catch (final ServerInitializerException e) {
				throw new GitServerRuntimeException(e);
			}
		}
	}
}
