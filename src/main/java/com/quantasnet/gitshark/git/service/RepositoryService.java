package com.quantasnet.gitshark.git.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quantasnet.gitshark.git.dfs.GitSharkDfsRepo;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.exception.RepositoryNotFoundException;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Service
public class RepositoryService {
	
	@Autowired
	private GitSharkDfsService dfsService;
	
	@Autowired
	private RepositoryUtilities repoUtils;
	
	public GitRepository getRepository(final String repoName, final String owner, final String userName) throws GitSharkException {
	final GitSharkDfsRepo repository = dfsService.getRepo(repoName, owner);
		
		if (null == repository) {
			throw new RepositoryNotFoundException(repoName);
		}
		
		// TODO check access
		return buildRepo(repository, owner);
	}

	private GitRepository buildRepo(final GitSharkDfsRepo repository, final String owner) throws GitSharkException {
		// TODO check anon perms
		final GitRepository repo = new GitRepository(dfsService, repository, owner, repository.getName(), false, false);
		repo.setCommits(repoUtils.hasCommits(repo));
		return repo;
	}
}
