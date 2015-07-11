package com.quantasnet.gitserver.git.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

import com.quantasnet.gitserver.git.repo.GitRepository;

public abstract class BaseCommit implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected final RevCommit commit;
	private final String commitUrl;
	private final List<Parent> parents = new ArrayList<>();
	
	public BaseCommit(final RevCommit commit, final GitRepository repo) {
		this.commit = commit;
		this.commitUrl = buildCommitUrl(repo);
		buildParents(repo);
	}
	
	protected abstract PersonIdent getCommitter();
	
	private String buildCommitUrl(final GitRepository repo) {
		if (null == commit) {
			return "";
		}
		
		return repo.getInterfaceBaseUrl() + "/commit/" + commit.getId().getName();
	}
	
	private void buildParents(final GitRepository repo) {
		if (null != commit) {
			if (commit.getParentCount() > 0) {
				for (final RevCommit parent : commit.getParents()) {
					parents.add(new Parent(parent.getId().getName(), repo));
				}
			}
		}
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
