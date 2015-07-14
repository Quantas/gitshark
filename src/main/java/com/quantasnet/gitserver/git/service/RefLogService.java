package com.quantasnet.gitserver.git.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.RefLog;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Service
public class RefLogService {

	private static final Logger LOG = LoggerFactory.getLogger(RefLogService.class);
	
	// Causes StackOverflowError on enormous pushes
	// @Cacheable(cacheNames = RepoCacheService.REFLOG, key = "#repo.fullDisplayName")
	public List<RefLog> retrieveActivity(final GitRepository repo) throws GitServerException {
		final List<RefLog> logs = new ArrayList<>();
		
		repo.execute(db -> {
			final Set<String> branches = db.getRefDatabase().getRefs(Constants.REFS_HEADS).keySet();
			
			final Git git = Git.wrap(db);
			
			branches.forEach(branch -> {
				try {
					logs.addAll(git.reflog().setRef(branch).call()
						.stream()
						.map(reflog -> buildRefLog(repo, db, branch, reflog))
						.collect(Collectors.toList()));
				} catch (final GitAPIException e) {
					LOG.error("There was an error generating the Reflog", e);
				}
			});
			
			Collections.sort(logs);
		});
		
		return logs;
	}

	private RefLog buildRefLog(GitRepository repo, Repository db, String branch, ReflogEntry reflog) {
		try {
			return new RefLog(reflog, repo, db, branch);
		} catch (final IOException e) {
			LOG.debug("Exception creating RefLog entry", e);
			return null;
		}
	}
	
}
