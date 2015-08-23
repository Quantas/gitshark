package com.quantasnet.gitshark.git.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitshark.Constants;
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
	private final ReceiveCommand.Type type;
	private final String comment;
	
	public RefLog(final GitSharkDfsRefLog refLog, final GitRepository repo, final Repository db) throws IOException {
		super(null, repo);
		this.type = ReceiveCommand.Type.valueOf(refLog.getType());
		this.branch = refLog.getBranch();
		this.totalCommitCount = generateCommits(refLog, db, repo);
		this.comment = parseCommentString();
		this.committer = new PersonIdent(refLog.getUserDisplayName(), refLog.getUserEmail(), refLog.getTime().toDate(), refLog.getTime().getZone().toTimeZone());
	}

	private long generateCommits(final GitSharkDfsRefLog refLog, final Repository db, final GitRepository repo) throws IOException {
		long commitCount = 0;

		if (type != ReceiveCommand.Type.DELETE && ! branch.startsWith(Constants.REFS_TAGS)) {
			try (final RevWalk revWalk = new RevWalk(db)) {

				final ObjectId newId = ObjectId.fromString(refLog.getNewId());
				final ObjectId oldId = ObjectId.fromString(refLog.getOldId());

				revWalk.markStart(revWalk.parseCommit(newId));

				for (final RevCommit commit : revWalk) {
					if (commit.getId().equals(oldId)) {
						break;
					}

					if (commitCount < 4) {
						commits.add(new Commit(commit, repo));
					}

					commitCount++;
				}
			}
		}
		return commitCount;
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

	private String parseCommentString() {
		final String theRef = branch.replaceFirst(Constants.REFS_HEADS, "").replaceFirst(Constants.REFS_TAGS, "");
		switch (type) {
			case CREATE:
				return branch.startsWith(Constants.REFS_HEADS) ? "Pushed New Branch " + theRef : "Pushed New Tag " + theRef;
			case DELETE:
				return "Deleted " + theRef;
			case UPDATE:
			case UPDATE_NONFASTFORWARD:
			default:
				return "Pushed " + totalCommitCount + " commit" + (totalCommitCount > 1 ? "s" : "") + " to " + theRef;
		}
	}


	@Override
	public int compareTo(final RefLog right) {
		return ComparisonChain.start()
				.compare(right.getCommitter().getWhen(), getCommitter().getWhen())
				.result();
	}
}
