package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.quantasnet.gitserver.git.repo.GitRepository;

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
	protected PersonIdent getCommitter() {
		return commit.getCommitterIdent();
	}
	
	public String getBranchHead() {
		return branchHead;
	}
}