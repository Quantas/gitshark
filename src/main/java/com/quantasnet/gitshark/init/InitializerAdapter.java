package com.quantasnet.gitshark.init;

import com.quantasnet.gitshark.git.exception.ServerInitializerException;

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
