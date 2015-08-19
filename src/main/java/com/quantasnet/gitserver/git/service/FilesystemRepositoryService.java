package com.quantasnet.gitserver.git.service;

import java.util.Map;

import org.eclipse.jgit.lib.Ref;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.cache.RepoCacheConstants;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Service
public class FilesystemRepositoryService {

	@Cacheable(cacheNames = RepoCacheConstants.REPO_SIZE, key = "#repo.fullDisplayName")
	public long repoSize(final GitRepository repo) {
		// TODO
		return 0;
	}

	@Cacheable(cacheNames = RepoCacheConstants.BRANCHES, key = "#repo.fullDisplayName")
	public Map<String, Ref> branches(final GitRepository repo) throws GitServerException {
		return repo.executeWithReturn(db -> db.getRefDatabase().getRefs(Constants.REFS_HEADS));
	}

	@Cacheable(cacheNames = RepoCacheConstants.TAGS, key = "#repo.fullDisplayName")
	public Map<String, Ref> tags(final GitRepository repo) throws GitServerException {
		return repo.executeWithReturn(db -> db.getRefDatabase().getRefs(Constants.REFS_TAGS));
	}
}
