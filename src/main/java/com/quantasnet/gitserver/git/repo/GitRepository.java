package com.quantasnet.gitserver.git.repo;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.dfs.GitSharkDfsRepository;
import com.quantasnet.gitserver.git.dfs.mongo.MongoRepo;
import com.quantasnet.gitserver.git.dfs.mongo.MongoService;
import com.quantasnet.gitserver.git.exception.GitSharkErrorException;
import com.quantasnet.gitserver.git.exception.GitSharkException;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class GitRepository implements Comparable<GitRepository> {

	private final MongoService mongoService;
	private final MongoRepo repository;
	private final String owner;
	private final String name;
	
	private final String displayName;
	private final String fullDisplayName;
	private final String interfaceBaseUrl;
	
	private final boolean anonRead;
	private final boolean anonWrite;
	
	private boolean commits;
	
	public GitRepository(final MongoService mongoService, final MongoRepo repository, final String owner, final String name, final boolean anonRead, final boolean anonWrite) {
		this.mongoService = mongoService;
		this.repository = repository;
		this.owner = owner;
		this.name = name;
		this.anonRead = anonRead;
		this.anonWrite = anonWrite;
		
		this.displayName = name.replaceAll("\\" + Constants.DOT_GIT_SUFFIX, "");
		this.fullDisplayName = getOwner() + '/' + getDisplayName();
		this.interfaceBaseUrl = getOwner() + '/' + getDisplayName();
	}
	
	public void execute(final RepositoryAction repoAction) throws GitSharkException {
		try (final Repository db = new GitSharkDfsRepository(repository.getId(), repository.getName(), mongoService)) {
			repoAction.doAction(db);
		} catch (final IOException e) {
			throw new GitSharkErrorException(e);
		}
	}

	public <T> T executeWithReturn(final RepositoryActionWithReturn<T> repoAction) throws GitSharkException {
		try (final Repository db = new GitSharkDfsRepository(repository.getId(), repository.getName(), mongoService)) {
			return repoAction.doAction(db);
		} catch (final IOException e) {
			throw new GitSharkErrorException(e);
		}
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
	
	public void setCommits(boolean commits) {
		this.commits = commits;
	}
	
	/**
	 * alias for isCommits
	 */
	public boolean hasCommits() {
		return isCommits();
	}
	
	public boolean isCommits() {
		return commits;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getFullDisplayName() {
		return fullDisplayName;
	}
	
	public String getInterfaceBaseUrl() {
		return interfaceBaseUrl;
	}

	@Override
	public String toString() {
		return "GitRepository [owner=" + owner + ", name=" + name + ", displayName=" + displayName
				+ ", fullDisplayName=" + fullDisplayName + ", anonRead=" + anonRead + ", anonWrite=" + anonWrite
				+ ", commits=" + commits + "]";
	}

	@Override
	public int compareTo(final GitRepository o) {
		return ComparisonChain.start()
			.compare(getOwner(), o.getOwner())
			.compare(getDisplayName(), o.getDisplayName())
			.result();
	}
}
