package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.RepositoryAccessDeniedException;
import com.quantasnet.gitserver.git.exception.RepositoryNotFoundException;

@Service
public class FilesystemRepositoryService {

	private static final Logger LOG = LoggerFactory.getLogger(FilesystemRepositoryService.class);
	
	private static final FileFilter GIT_ONLY_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getAbsolutePath().endsWith(Constants.DOT_GIT_SUFFIX);
		}
	};
	
	@Autowired
	private RepoFolderUtil folderUtil;
	
	public GitRepository getRepository(final String userName, final String owner, final String repoName) throws RepositoryNotFoundException, RepositoryAccessDeniedException {
		// if (owner.equals(userName)) {
			final File gitFolder = folderUtil.getRepoDir(owner, endsWithGit(repoName));
			if (gitFolder.exists() && gitFolder.isDirectory()) {
				return new GitRepository(gitFolder, owner, gitFolder.getName(), true, false); // TODO fix
			}
			throw new RepositoryNotFoundException(gitFolder.getName());
		// }
		
		// throw new RepositoryAccessDeniedException();
	}
	
	public List<GitRepository> getRepositories(final String owner) {
		final File rootFolder = folderUtil.getOwnerRootDir(owner);
		final List<GitRepository> repos = new ArrayList<>();
		LOG.debug("ROOT={}", rootFolder);
		if (rootFolder.isDirectory()) {
			// get all the child folders
			for (final File child : rootFolder.listFiles(GIT_ONLY_FILTER)) {
				LOG.debug("CHILD={}", child);
				repos.add(new GitRepository(child, owner, child.getName(), true, false)); // TODO fix
			}
		}
		
		Collections.sort(repos);
		
		return repos;
	}
	
	public GitRepository createRepo(final String name, final String owner) throws IOException {
		final File rootFolder = folderUtil.getOwnerRootDir(owner);
		final File newRepo = new File(rootFolder, endsWithGit(name));
		
		try {
			final Git repo = Git.init().setGitDir(newRepo).setBare(true).call();
			repo.getRepository().getConfig().setBoolean("core", null, "logAllRefUpdates", true);
		} catch (IllegalStateException | GitAPIException e) {
			throw new RuntimeException(e);
		}
		
		return new GitRepository(newRepo, owner, name, true, false); // TODO fix
	}
	
	private String endsWithGit(final String name) {
		if (!name.endsWith(Constants.DOT_GIT_SUFFIX)) {
			return name + Constants.DOT_GIT_SUFFIX;
		}
		return name;
	}
}
