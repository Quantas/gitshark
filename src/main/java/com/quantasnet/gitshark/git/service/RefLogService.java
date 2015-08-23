package com.quantasnet.gitshark.git.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.quantasnet.gitshark.git.cache.RepoCacheConstants;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRefLog;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.RefLog;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Service
public class RefLogService {

	private static final Logger LOG = LoggerFactory.getLogger(RefLogService.class);
	
	@Autowired
	private GitSharkDfsService dfsService;
	
	@Cacheable(cacheNames = RepoCacheConstants.REFLOG, key = "#repo.fullDisplayName")
	public List<RefLog> retrieveActivity(final GitRepository repo) throws GitSharkException {
		return repo.executeWithReturn(db -> {
			final List<RefLog> logs = dfsService.getRefLogForRepo(repo.getId())
					.stream()
					.map(refLog -> buildRefLog(repo, db, refLog))
					.collect(Collectors.toList());
			
			Collections.sort(logs);
			return logs;
		});
	}

	private RefLog buildRefLog(final GitRepository repo, final Repository db, final GitSharkDfsRefLog refLog) {
		try {
			return new RefLog(refLog, repo, db);
		} catch (final IOException e) {
			LOG.error("Exception creating RefLog entry", e);
			return null;
		}
	}
	
}
