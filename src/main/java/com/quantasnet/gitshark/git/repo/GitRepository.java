package com.quantasnet.gitshark.git.repo;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRepo;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRepository;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.dfs.GitSharkRepoSecurity;
import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class GitRepository implements Comparable<GitRepository> {

	private final GitSharkDfsService dfsService;
	private final GitSharkDfsRepo repository;
	private final GitSharkRepoSecurity security;
	private final String owner;
	private final String name;
	
	private final String displayName;
	private final String fullDisplayName;
	private final String interfaceBaseUrl;
	
	private boolean commits;
	
	public GitRepository(final GitSharkDfsService dfsService, final GitSharkDfsRepo repository, final String owner, final String name, final GitSharkRepoSecurity security) {
		this.dfsService = dfsService;
		this.repository = repository;
		this.owner = owner;
		this.name = name;
		this.security = security;
		
		this.displayName = name.replaceAll("\\" + Constants.DOT_GIT_SUFFIX, "");
		this.fullDisplayName = getOwner() + '/' + getDisplayName();
		this.interfaceBaseUrl = getOwner() + '/' + getDisplayName();
	}
	
	public void execute(final RepositoryAction repoAction) throws GitSharkException {
		try (final Repository db = new GitSharkDfsRepository(repository.getId(), repository.getName(), dfsService)) {
			repoAction.doAction(db);
		} catch (final IOException e) {
			throw new GitSharkErrorException(e);
		}
	}

	public <T> T executeWithReturn(final RepositoryActionWithReturn<T> repoAction) throws GitSharkException {
		try (final Repository db = new GitSharkDfsRepository(repository.getId(), repository.getName(), dfsService)) {
			return repoAction.doAction(db);
		} catch (final IOException e) {
			throw new GitSharkErrorException(e);
		}
	}
	
	public String getId() {
		return repository.getId();
	}
	
	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public GitSharkRepoSecurity getSecurity() {
		return security;
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
				+ ", fullDisplayName=" + fullDisplayName + ", security=" + security
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
