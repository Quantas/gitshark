package com.quantasnet.gitserver.init;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ServerInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(ServerInitializer.class);
	
	private static boolean alreadyInit = false;
	
	@Autowired
	private List<Initializer> initializers;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (alreadyInit) {
			final String message = "SERVER ALREADY INITALIZED...";
			LOG.error(message);
			throw new RuntimeException(message);
		} else {
			try {
				for (final Initializer init : initializers) {
					init.init();
				}
				alreadyInit = true;
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
