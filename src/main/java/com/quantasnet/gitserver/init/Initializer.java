package com.quantasnet.gitserver.init;

import com.quantasnet.gitserver.git.exception.ServerInitializerException;


public interface Initializer {
	void init() throws ServerInitializerException;
	void stop();
}
