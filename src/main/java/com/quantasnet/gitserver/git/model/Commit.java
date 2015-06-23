package com.quantasnet.gitserver.git.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.quantasnet.gitserver.git.repo.GitRepository;

public class Commit extends BaseCommit {

	private final String branchHead;
	
	public Commit(final RevCommit commit, final GitRepository repo) {
		this(commit, repo, null);
	}
	
	public Commit(final RevCommit commit, final GitRepository repo, final String branchHead) {
		super(commit, repo);
		this.branchHead = branchHead;
	}
	
	public String getCommitterName() {
		return commit.getCommitterIdent().getName();
	}
	
	public String getCommitterEmail() {
		return commit.getCommitterIdent().getEmailAddress();
	}
	
	public String getBranchHead() {
		return branchHead;
	}
	
	public String getGravatarUrl() {
		final String email = commit.getCommitterIdent().getEmailAddress();
		final String theEmail = null == email ? "" : email;
		return "//www.gravatar.com/avatar/" + DigestUtils.md5Hex(theEmail.trim().toLowerCase()) + "?d=identicon&rating=g";
	}
}