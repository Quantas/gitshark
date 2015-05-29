package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryService.class);
	
	private static final FileFilter FOLDERS_ONLY_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};
	
	@Autowired
	private RepoFolderUtil folderUtil;
	
	public Set<GitRepository> getRepositories() {
		final Set<GitRepository> repos = new HashSet<>();
		
		repos.addAll(getRepositories(RepoType.USER, new File(folderUtil.getUserReposRoot())));
		repos.addAll(getRepositories(RepoType.PROJECT, new File(folderUtil.getProjectReposRoot())));
		
		return repos;
	}
	
	private Set<GitRepository> getRepositories(final RepoType type, final File folder) {
		final Set<GitRepository> repos = new HashSet<>();
		LOG.debug("ROOT={}", folder);
		if (folder.isDirectory()) {
			// get all the child folders
			for (final File parent : folder.listFiles(FOLDERS_ONLY_FILTER)) {
				LOG.debug("CHILD={}", parent);
				for (final File repo : parent.listFiles(FOLDERS_ONLY_FILTER)) {
					repos.add(new GitRepository(repo, type, parent.getName(), repo.getName()));
				}
			}
		}
		return repos;
	}
}
