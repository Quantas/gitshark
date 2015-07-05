package com.quantasnet.gitserver.init;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantasnet.gitserver.git.exception.ServerInitializerException;

public abstract class FolderInitializer extends InitializerAdapter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected void initializeDirectory(final String dir, final String name) throws ServerInitializerException {
		final File folder = new File(dir);
		
		logger.info("Checking folder: {}", folder.getAbsolutePath());
		
		final boolean rootExists = folder.exists();
		logger.info("{} exists? {}", name, rootExists);
		
		if (!rootExists) {
			final boolean created = folder.mkdirs();
			logger.info("{} created? {}", name, created);
		}
		
		if (!folder.canWrite()) {
			logger.error("ERROR can not write {}, {}", name, folder);
			throw new ServerInitializerException("ERROR can not write " + name + ", " + folder);
		}
	}

}
