package com.quantasnet.gitserver.git.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.exception.RepositoryNotFoundException;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.repo.RepoFolderUtil;

@Service
public class FilesystemRepositoryService {

	private static final Logger LOG = LoggerFactory.getLogger(FilesystemRepositoryService.class);
	
	private static final FileFilter GIT_ONLY_FILTER = pathname -> pathname.getAbsolutePath().endsWith(Constants.DOT_GIT_SUFFIX);
	
	@Autowired
	private RepoFolderUtil folderUtil;

	@Autowired
	private RepositoryUtilities repoUtilities;
	
	public GitRepository getRepository(final String userName, final String owner, final String repoName) throws GitServerException {
		// if (owner.equals(userName)) {
			final File gitFolder = folderUtil.getRepoDir(owner, endsWithGit(repoName));
			if (gitFolder.exists() && gitFolder.isDirectory()) {
				final GitRepository repo = new GitRepository(gitFolder, owner, gitFolder.getName(), true, false); // TODO fix
				repo.setCommits(repoUtilities.hasCommits(repo));
				return repo;
			}
			throw new RepositoryNotFoundException(gitFolder.getName());
		// }
		
		// throw new RepositoryAccessDeniedException();
	}
	
	public List<String> getOwners() {
		return Arrays.asList(new File(folderUtil.getReposRoot()).listFiles())
				.stream().map(File::getName).collect(Collectors.toList());
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
	
	public GitRepository createRepo(final String name, final String owner) throws GitServerException {
		final File rootFolder = folderUtil.getOwnerRootDir(owner);
		final File newRepo = new File(rootFolder, endsWithGit(name));
		
		try {
			final Git repo = Git.init().setGitDir(newRepo).setBare(true).call();
			final StoredConfig config = repo.getRepository().getConfig();
			config.setBoolean("core", null, "logAllRefUpdates", true);
			config.save();
		} catch (final IOException | IllegalStateException | GitAPIException e) {
			throw new GitServerErrorException(e);
		}
		
		return new GitRepository(newRepo, owner, name, true, false); // TODO fix
	}
	
	public boolean deleteRepo(final GitRepository repo) {
		// TODO check ownership/permissions here
		try {
			FileUtils.deleteDirectory(repo.getFullRepoDirectory());
			return true;
		} catch (final IOException e) {
			LOG.error("Error deleting repo {}", repo.getFullRepoDirectory(), e);
			return false;
		}
	}
	
	private String endsWithGit(final String name) {
		if (!name.endsWith(Constants.DOT_GIT_SUFFIX)) {
			return name + Constants.DOT_GIT_SUFFIX;
		}
		return name;
	}
}