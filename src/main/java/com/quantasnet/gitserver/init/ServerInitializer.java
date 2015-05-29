package com.quantasnet.gitserver.init;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ServerInitializer implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private List<Initializer> initializers;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			for (final Initializer init : initializers) {
				init.init();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
