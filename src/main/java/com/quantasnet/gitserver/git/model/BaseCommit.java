package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

public abstract class BaseCommit {

	protected final RevCommit commit;
	
	public BaseCommit(final RevCommit commit) {
		this.commit = commit;
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
	
	public String getDateTimeString() {
		if (null != commit) {
			return new DateTime(commit.getCommitterIdent().getWhen().getTime()).toString("yyyy-MM-dd'T'HH:mm:ssZ");
		}
		
		return null;
	}
}
