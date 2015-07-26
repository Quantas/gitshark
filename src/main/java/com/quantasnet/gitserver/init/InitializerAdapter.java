package com.quantasnet.gitserver.init;

import com.quantasnet.gitserver.git.exception.ServerInitializerException;

public abstract class InitializerAdapter implements Initializer {

	@Override
	public void init() throws ServerInitializerException {
		// default no-op
	}
	
	@Override
	public void stop() {
		// default no-op
	}
	
}
