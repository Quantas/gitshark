package com.quantasnet.gitshark.init;

import com.quantasnet.gitshark.git.exception.ServerInitializerException;


public interface Initializer {
	void init() throws ServerInitializerException;
	void stop();
}
