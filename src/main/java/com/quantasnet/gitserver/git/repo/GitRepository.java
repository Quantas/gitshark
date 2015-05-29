package com.quantasnet.gitserver.git.repo;

import java.io.File;

public class GitRepository {

	private final File fullRepoDirectory;
	private final RepoType type;
	private final String owner;
	private final String name;
	
	public GitRepository(final File fullRepoDirectory, final RepoType type, final String owner, final String name) {
		this.fullRepoDirectory = fullRepoDirectory;
		this.type = type;
		this.owner = owner;
		this.name = name;
	}

	public File getFullRepoDirectory() {
		return fullRepoDirectory;
	}

	public RepoType getType() {
		return type;
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
				+ ", type=" + type + ", owner=" + owner + ", name=" + name
				+ "]";
	}
	
}
