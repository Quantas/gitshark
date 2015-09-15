package com.quantasnet.gitshark.init;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.quantasnet.gitshark.git.exception.ServerInitializerException;
import com.quantasnet.gitshark.git.protocol.git.GitProtocolService;

@Profile("!cloud")
@Order(InitOrdering.GIT_PROTO)
@Component
public class GitProtocolInitalizer implements Initializer {

	@Autowired
	private GitProtocolService gitProtocolService;
	
	@Override
	public void init() throws ServerInitializerException {
		try {
			gitProtocolService.start();
		} catch (final IOException e) {
			throw new ServerInitializerException(e);
		}
	}
	
	@Override
	public void stop() {
		gitProtocolService.stop();
	}

}
