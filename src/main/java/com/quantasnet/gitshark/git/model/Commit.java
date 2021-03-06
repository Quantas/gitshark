package com.quantasnet.gitshark.git.model;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.quantasnet.gitshark.git.repo.GitRepository;

public class Commit extends BaseCommit {
	
	private static final long serialVersionUID = 1L;
	
	private final String branchHead;
	
	public Commit(final RevCommit commit, final GitRepository repo) {
		this(commit, repo, null);
	}
	
	public Commit(final RevCommit commit, final GitRepository repo, final String branchHead) {
		super(commit, repo);
		this.branchHead = branchHead;
	}
	
	@Override
	public PersonIdent getCommitter() {
		return commit.getCommitterIdent();
	}

	@Override
	public PersonIdent getAuthor() {
		return commit.getAuthorIdent();
	}

	public String getBranchHead() {
		return branchHead;
	}
}