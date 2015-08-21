package com.quantasnet.gitshark.git.service;

import java.util.Map;

import org.eclipse.jgit.lib.Ref;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.cache.RepoCacheConstants;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Service
public class RefService {

	@Cacheable(cacheNames = RepoCacheConstants.BRANCHES, key = "#repo.fullDisplayName")
	public Map<String, Ref> branches(final GitRepository repo) throws GitSharkException {
		return repo.executeWithReturn(db -> db.getRefDatabase().getRefs(Constants.REFS_HEADS));
	}

	@Cacheable(cacheNames = RepoCacheConstants.TAGS, key = "#repo.fullDisplayName")
	public Map<String, Ref> tags(final GitRepository repo) throws GitSharkException {
		return repo.executeWithReturn(db -> db.getRefDatabase().getRefs(Constants.REFS_TAGS));
	}
}
