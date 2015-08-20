package com.quantasnet.gitshark.git.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.RefLog;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Service
public class RefLogService {

	private static final Logger LOG = LoggerFactory.getLogger(RefLogService.class);
	
	@Autowired
	private FilesystemRepositoryService repoService;
	
	// TODO completely broken when using DFS
	
	// Causes StackOverflowError on enormous pushes
	// @Cacheable(cacheNames = RepoCacheConstants.REFLOG, key = "#repo.fullDisplayName")
	public List<RefLog> retrieveActivity(final GitRepository repo) throws GitSharkException {
		final List<RefLog> logs = new ArrayList<>();
		
		repo.execute(db -> {
			final Set<String> branches = repoService.branches(repo).keySet();
			
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

	private RefLog buildRefLog(final GitRepository repo, final Repository db, final String branch, final ReflogEntry reflog) {
		try {
			return new RefLog(reflog, repo, db, branch);
		} catch (final IOException e) {
			LOG.debug("Exception creating RefLog entry", e);
			return null;
		}
	}
	
}
