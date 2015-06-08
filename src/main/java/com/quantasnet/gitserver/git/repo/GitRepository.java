package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import com.quantasnet.gitserver.Constants;

public class GitRepository {

	private final File fullRepoDirectory;
	private final String owner;
	private final String name;
	
	public GitRepository(final File fullRepoDirectory, final String owner, final String name) {
		this.fullRepoDirectory = fullRepoDirectory;
		this.owner = owner;
		this.name = name;
	}
	
	public Repository getDB() throws IOException {
		return Git.open(getFullRepoDirectory()).getRepository();
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
	
	public String getDisplayName() {
		return name.replaceAll("\\" + Constants.DOT_GIT_SUFFIX, "");
	}

	@Override
	public String toString() {
		return "GitRepository [fullRepoDirectory=" + fullRepoDirectory
				+ ", owner=" + owner + ", name=" + name + "]";
	}
}
