package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryService.class);
	
	private static final FileFilter GIT_ONLY_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getAbsolutePath().endsWith(".git");
		}
	};
	
	@Autowired
	private RepoFolderUtil folderUtil;
	
	public Set<GitRepository> getRepositories(final String owner) {
		final File rootFolder = folderUtil.getRepoDir(owner);
		final Set<GitRepository> repos = new HashSet<>();
		LOG.info("ROOT={}", rootFolder);
		if (rootFolder.isDirectory()) {
			// get all the child folders
			for (final File child : rootFolder.listFiles(GIT_ONLY_FILTER)) {
				LOG.info("CHILD={}", child);
				repos.add(new GitRepository(child, owner, child.getName()));
			}
		}
		return repos;
	}
	
	public GitRepository createRepo(final String name, final String owner) throws IOException {
		final File rootFolder = folderUtil.getRepoDir(owner);
		final File newRepo = new File(rootFolder, name + ".git");
		
		try {
			/*final Git git = */ Git.init().setGitDir(newRepo).setBare(false).call();
			//final Repository repo = git.getRepository();
		} catch (IllegalStateException | GitAPIException e) {
			throw new RuntimeException(e);
		}
		
		return new GitRepository(newRepo, owner, name);
	}
}
