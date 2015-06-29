package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.quantasnet.gitserver.git.repo.GitRepository;

public class RefHolder extends BaseCommit {

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

}
