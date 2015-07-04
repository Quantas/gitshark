package com.quantasnet.gitserver.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.exception.ServerInitializerException;
import com.quantasnet.gitserver.git.repo.FilesystemRepositoryService;

@Component
public class RepoConfigInitializer implements Initializer {

	private static final Logger LOG = LoggerFactory.getLogger(RepoConfigInitializer.class);
	
	@Autowired
	private FilesystemRepositoryService service;
	
	@Override
	public void init() throws ServerInitializerException {
		service.getOwners().forEach(owner -> 
			service.getRepositories(owner).forEach(repo -> {
				try {
					repo.execute(db -> {
						final boolean isSet = db.getConfig().getBoolean("core", null, "logAllRefUpdates", false);
						
						LOG.info("repo {} logAllRefUpdates={}", repo.getFullDisplayName(), isSet);
						
						if (!isSet) {
							db.getConfig().setBoolean("core", null, "logAllRefUpdates", true);
							db.getConfig().save();
						}
					});
				} catch (final Exception e) {
					LOG.error("There was a terrible error setting repo configs", e);
				}
			})
		);
	}

	@Override
	public void stop() {
	}

}
