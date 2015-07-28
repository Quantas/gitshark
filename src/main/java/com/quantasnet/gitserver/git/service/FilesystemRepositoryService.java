package com.quantasnet.gitserver.git.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;
import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.exception.RepositoryAccessDeniedException;
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
	
	public GitRepository getRepository(final String userName, final String owner, final String repoName) throws GitServerException, RepositoryAccessDeniedException {
		if (owner.equals(userName)) {
			final File gitFolder = folderUtil.getRepoDir(owner, endsWithGit(repoName));
			if (gitFolder.exists() && gitFolder.isDirectory()) {
				return buildRepo(gitFolder, owner, gitFolder.getName());
			}
			throw new RepositoryNotFoundException(gitFolder.getName());
		}
		
		throw new RepositoryAccessDeniedException();
	}
	
	public List<GitRepository> getRepositories(final String owner) throws GitServerException {
		final File rootFolder = folderUtil.getOwnerRootDir(owner);
		final List<GitRepository> repos = new ArrayList<>();
		LOG.debug("ROOT={}", rootFolder);
		if (rootFolder.isDirectory()) {
			// get all the child folders
			for (final File child : rootFolder.listFiles(GIT_ONLY_FILTER)) {
				LOG.debug("CHILD={}", child);
				repos.add(buildRepo(child, owner, child.getName()));
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
		
		return buildRepo(newRepo, owner, name);
	}

	@Cacheable(cacheNames = RepoCacheService.REPO_SIZE, key = "#repo.fullDisplayName")
	public long repoSize(final GitRepository repo) {
		final Iterable<File> files = Files.fileTreeTraverser().breadthFirstTraversal(repo.getFullRepoDirectory());
		return StreamSupport.stream(files.spliterator(), false).mapToLong(File::length).sum();
	}

	@Cacheable(cacheNames = RepoCacheService.BRANCHES, key = "#repo.fullDisplayName")
	public Set<String> branches(final GitRepository repo) throws GitServerException {
		return repo.executeWithReturn(db -> db.getRefDatabase().getRefs(Constants.REFS_HEADS).keySet());
	}

	@Cacheable(cacheNames = RepoCacheService.TAGS, key = "#repo.fullDisplayName")
	public Set<String> tags(final GitRepository repo) throws GitServerException {
		return repo.executeWithReturn(db -> db.getRefDatabase().getRefs(Constants.REFS_TAGS).keySet());
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

	private GitRepository buildRepo(final File dir, final String owner, final String name) throws GitServerException {
		// TODO get correct anon settings
		final GitRepository repo = new GitRepository(dir, owner, name, false, false);
		repo.setCommits(repoUtilities.hasCommits(repo));
		return repo;
	}

	private String endsWithGit(final String name) {
		if (!name.endsWith(Constants.DOT_GIT_SUFFIX)) {
			return name + Constants.DOT_GIT_SUFFIX;
		}
		return name;
	}
}
