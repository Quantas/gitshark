package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class RefHolder extends BaseCommit implements Comparable<RefHolder> {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	public RefHolder(final RevCommit commit, final GitRepository repo, final String name) {
		super(commit, repo);
		this.name = name;
	}

	@Override
	protected PersonIdent getCommitter() {
		return commit.getCommitterIdent();
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(final RefHolder right) {
		return ComparisonChain.start()
				.compare(right.getCommitter().getWhen(), getCommitter().getWhen())
				.result();
	}
}
