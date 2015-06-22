package com.quantasnet.gitserver.git.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

import com.quantasnet.gitserver.git.repo.GitRepository;

public abstract class BaseCommit {

	protected final RevCommit commit;
	private final String commitUrl;
	private final List<Parent> parents;
	private final String dateTimeString;
	
	public BaseCommit(final RevCommit commit, final GitRepository repo) {
		this.commit = commit;
		this.commitUrl = buildCommitUrl(repo);
		this.parents = buildParents(repo);
		this.dateTimeString = buildDateTimeString();
	}
	
	private String buildCommitUrl(final GitRepository repo) {
		if (null == commit) {
			return "";
		}
		
		return repo.getInterfaceBaseUrl() + "/commit/" + commit.getId().getName();
	}
	
	private List<Parent> buildParents(final GitRepository repo) {
		final List<Parent> parents = new ArrayList<>();
		
		if (null != commit) {
			if (commit.getParentCount() > 0) {
				for (final RevCommit parent : commit.getParents()) {
					parents.add(new Parent(parent.getId().getName(), repo));
				}
			}
		}
		
		return parents;
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
	
	public List<Parent> getParents() {
		return parents;
	}
	
	public boolean isMerge() {
		return parents.size() > 1;
	}
	
	public String getDateTimeString() {
		return dateTimeString;
	}
}
