package com.quantasnet.gitserver.git.repo;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class GitRepository {

	private final File fullRepoDirectory;
	private final String owner;
	private final String name;
	
	public GitRepository(final File fullRepoDirectory, final String owner, final String name) {
		this.fullRepoDirectory = fullRepoDirectory;
		this.owner = owner;
		this.name = name;
	}

	public static void execute(final GitRepository repo, final RepositoryAction repoAction) throws Exception {
		
		Repository db = null;
		
		try {
			db = Git.open(repo.getFullRepoDirectory()).getRepository();
			repoAction.doAction(db);
		} finally {
			if (null != db) {
				db.close();
			}
		}
		
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

	@Override
	public String toString() {
		return "GitRepository [fullRepoDirectory=" + fullRepoDirectory
				+ ", owner=" + owner + ", name=" + name + "]";
	}
	
}
