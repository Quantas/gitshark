package com.quantasnet.gitserver.git.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.revwalk.RevCommit;

public class Commit extends BaseCommit {

	public Commit(final RevCommit commit) {
		super(commit);
	}
	
	public String getId() {
		return commit.getId().getName();
	}
	
	public String getCommitterName() {
		return commit.getCommitterIdent().getName();
	}
	
	public String getCommitterEmail() {
		return commit.getCommitterIdent().getEmailAddress();
	}
	
	public String getGravatarUrl() {
		final String email = commit.getCommitterIdent().getEmailAddress();
		final String theEmail = null == email ? "" : email;
		return "//www.gravatar.com/avatar/" + DigestUtils.md5Hex(theEmail.trim().toLowerCase()) + "?d=identicon&rating=g";
	}
}