package com.quantasnet.gitserver.init;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FolderInitializer implements Initializer {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected void initializeDirectory(final String dir, final String name) throws FileNotFoundException {
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
			throw new FileNotFoundException("ERROR can not write " + name + ", " + folder);
		}
	}

}
