package com.quantasnet.gitserver.git.repo;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitserver.Constants;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class GitRepository implements Comparable<GitRepository> {

	private final File fullRepoDirectory;
	private final String owner;
	private final String name;
	
	private final String displayName;
	private final String interfaceBaseUrl;
	
	private final boolean anonRead;
	private final boolean anonWrite;
	
	public GitRepository(final File fullRepoDirectory, final String owner, final String name, final boolean anonRead, final boolean anonWrite) {
		this.fullRepoDirectory = fullRepoDirectory;
		this.owner = owner;
		this.name = name;
		this.anonRead = anonRead;
		this.anonWrite = anonWrite;
		
		this.displayName = name.replaceAll("\\" + Constants.DOT_GIT_SUFFIX, "");
		this.interfaceBaseUrl = getOwner() + '/' + getDisplayName();
	}
	
	public void execute(final RepositoryAction repoAction) throws Exception {
		try (final Repository db = Git.open(getFullRepoDirectory()).getRepository()) {
			repoAction.doAction(db);
		}
	}
	
	public boolean hasCommits(final Repository repository) {
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
		return displayName;
	}
	
	public String getInterfaceBaseUrl() {
		return interfaceBaseUrl;
	}

	@Override
	public String toString() {
		return "GitRepository [fullRepoDirectory=" + fullRepoDirectory
			+ ", owner=" + owner + ", name=" + name + ", anonRead="
			+ anonRead + ", anonWrite=" + anonWrite + "]";
	}

	@Override
	public int compareTo(final GitRepository o) {
		return ComparisonChain.start()
			.compare(getOwner(), o.getOwner())
			.compare(getDisplayName(), o.getDisplayName())
			.result();
	}
}
