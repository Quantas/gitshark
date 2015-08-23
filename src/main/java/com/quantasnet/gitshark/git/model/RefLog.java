package com.quantasnet.gitshark.git.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRefLog;
import com.quantasnet.gitshark.git.repo.GitRepository;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class RefLog extends BaseCommit implements Comparable<RefLog> {

	private static final long serialVersionUID = 1L;
	
	private final List<Commit> commits = new ArrayList<>();
	private final long totalCommitCount;
	private final String branch;
	private final PersonIdent committer;
	private final String comment;
	
	public RefLog(final GitSharkDfsRefLog refLog, final GitRepository repo, final Repository db) throws IOException {
		super(null, repo);
		this.totalCommitCount = generateCommits(refLog, db, repo);
		this.branch = refLog.getBranch();
		this.comment = "Pushed " + totalCommitCount + " commit" + (totalCommitCount > 1 ? "s" : "") + " to " + branch;
		this.committer = new PersonIdent(refLog.getUserDisplayName(), refLog.getUserEmail(), refLog.getTime().toDate(), refLog.getTime().getZone().toTimeZone());
	}

	private long generateCommits(final GitSharkDfsRefLog refLog, final Repository db, final GitRepository repo) throws IOException {
		try (final RevWalk revWalk = new RevWalk(db)) {
			
			final ObjectId newId = ObjectId.fromString(refLog.getNewId());
			final ObjectId oldId = ObjectId.fromString(refLog.getOldId());
			
			revWalk.markStart(revWalk.parseCommit(newId));
			
			long commitCount = 0;
			
			for (final RevCommit commit : revWalk) {
				if (commit.getId().equals(oldId)) {
					break;
				}
				
				if (commitCount < 4) {
					commits.add(new Commit(commit, repo));
				}
				
				commitCount++;
			}
			
			return commitCount;
		}
	}
	
	@Override
	public PersonIdent getCommitter() {
		return committer;
	}

	@Override
	public PersonIdent getAuthor() {
		return committer;
	}

	public List<Commit> getCommits() {
		return commits;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String getBranch() {
		return branch;
	}

	public long getTotalCommitCount() {
		return totalCommitCount;
	}
	
	@Override
	public int compareTo(final RefLog right) {
		return ComparisonChain.start()
				.compare(right.getCommitter().getWhen(), getCommitter().getWhen())
				.result();
	}
}
