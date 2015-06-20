package com.quantasnet.gitserver.init;


public interface Initializer {
	void init() throws Exception;
	void stop();
}
