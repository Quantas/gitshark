package com.quantasnet.gitserver.git.repo;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import com.quantasnet.gitserver.Constants;

public class GitRepository {

	private final File fullRepoDirectory;
	private final String owner;
	private final String name;
	
	private final boolean anonRead;
	private final boolean anonWrite;
	
	public GitRepository(final File fullRepoDirectory, final String owner, final String name, final boolean anonRead, final boolean anonWrite) {
		this.fullRepoDirectory = fullRepoDirectory;
		this.owner = owner;
		this.name = name;
		this.anonRead = anonRead;
		this.anonWrite = anonWrite;
	}
	
	public static void execute(final GitRepository repo, final RepositoryAction repoAction) throws Exception {
		try (final Repository db = Git.open(repo.getFullRepoDirectory()).getRepository()) {
			repoAction.doAction(db);
		}
	}
	
	public static boolean hasCommits(Repository repository) {
		if (repository != null && repository.getDirectory().exists()) {
			return (new File(repository.getDirectory(), "objects").list().length > 2)
					|| (new File(repository.getDirectory(), "objects/pack").list().length > 0);
		}
		return false;
	}
	
	public File getFullRepoDirectory() {
		return fullRepoDirectory;
	}

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}
	
	public boolean isAnonRead() {
		return anonRead;
	}
	
	public boolean isAnonWrite() {
		return anonWrite;
	}
	
	public String getDisplayName() {
		return name.replaceAll("\\" + Constants.DOT_GIT_SUFFIX, "");
	}
	
	public String getInterfaceBaseUrl() {
		return getOwner() + '/' + getDisplayName();
	}

	@Override
	public String toString() {
		return "GitRepository [fullRepoDirectory=" + fullRepoDirectory
				+ ", owner=" + owner + ", name=" + name + "]";
	}
}
