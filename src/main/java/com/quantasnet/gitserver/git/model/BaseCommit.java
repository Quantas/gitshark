package com.quantasnet.gitserver.git.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

import com.quantasnet.gitserver.git.repo.GitRepository;

public abstract class BaseCommit {

	protected final RevCommit commit;
	private final String commitUrl;
	private final List<Parent> parents;
	
	protected abstract PersonIdent getCommitter();
	
	public BaseCommit(final RevCommit commit, final GitRepository repo) {
		this.commit = commit;
		this.commitUrl = buildCommitUrl(repo);
		this.parents = buildParents(repo);
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
	
	public String getDateTimeString() {
		return new DateTime(getCommitter().getWhen().getTime()).toString("yyyy-MM-dd'T'HH:mm:ssZ");
	}
	
	public String getCommitterName() {
		return getCommitter().getName();
	}
	
	public String getCommitterEmail() {
		return getCommitter().getEmailAddress();
	}
	
	public String getGravatarUrl() {
		final String email = getCommitter().getEmailAddress();
		final String theEmail = null == email ? "" : email;
		return "//www.gravatar.com/avatar/" + DigestUtils.md5Hex(theEmail.trim().toLowerCase()) + "?d=identicon&rating=g";
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
}
