package com.quantasnet.gitserver.init;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ServerDeInitializer implements ApplicationListener<ContextClosedEvent> {

	@Autowired
	private List<Initializer> initializers;
	
	@Override
	public void onApplicationEvent(final ContextClosedEvent event) {
		initializers.forEach(Initializer::stop);
	}
}
