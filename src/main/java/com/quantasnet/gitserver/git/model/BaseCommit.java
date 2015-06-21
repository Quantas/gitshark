package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

import com.quantasnet.gitserver.git.repo.GitRepository;

public abstract class BaseCommit {

	protected final RevCommit commit;
	private final String commitUrl;
	private final String dateTimeString;
	
	public BaseCommit(final RevCommit commit, final GitRepository repo) {
		this.commit = commit;
		this.commitUrl = buildCommitUrl(repo);
		this.dateTimeString = buildDateTimeString();
	}
	
	private String buildCommitUrl(final GitRepository repo) {
		if (null == commit) {
			return "";
		}
		
		return repo.getInterfaceBaseUrl() + "/commit/" + commit.getId().getName();
	}
	
	private String buildDateTimeString() {
		if (null == commit) {
			return "";
		}
		
		return new DateTime(commit.getCommitterIdent().getWhen().getTime()).toString("yyyy-MM-dd'T'HH:mm:ssZ");
	}
	
	public RevCommit getCommit() {
		return commit;
	}
	
	public String getId() {
		return commit.getId().getName();
	}
	
	public String getShortId() {
		return commit.getId().getName().substring(0, 7);
	}
	
	public String getCommitUrl() {
		return commitUrl;
	}
	
	public String getDateTimeString() {
		return dateTimeString;
	}
}
