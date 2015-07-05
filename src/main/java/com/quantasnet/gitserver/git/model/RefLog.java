package com.quantasnet.gitserver.git.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class RefLog extends BaseCommit implements Comparable<RefLog> {

	private final ReflogEntry reflogEntry;
	private final List<Commit> commits = new ArrayList<>();
	private final String branch;
	
	public RefLog(final ReflogEntry reflogEntry, final GitRepository repo, final Repository db, final String branch) throws IOException {
		super(null, repo);
		this.reflogEntry = reflogEntry;
		generateCommits(reflogEntry, db, repo);
		this.branch = branch;
	}

	private void generateCommits(final ReflogEntry reflogEntry, final Repository db, final GitRepository repo) throws IOException {
		try (final RevWalk revWalk = new RevWalk(db)) {
			
			final ObjectId newId = reflogEntry.getNewId();
			final ObjectId oldId = reflogEntry.getOldId();
			
			revWalk.markStart(revWalk.parseCommit(newId));
			
			for (final RevCommit commit : revWalk) {
				if (commit.getId().equals(oldId)) {
					break;
				}
				
				commits.add(new Commit(commit, repo));
			}
		}
	}
	
	@Override
	protected PersonIdent getCommitter() {
		return reflogEntry.getWho();
	}
	
	public List<Commit> getCommits() {
		return commits;
	}
	
	public String getComment() {
		return reflogEntry.getComment();
	}
	
	public String getBranch() {
		return branch;
	}

	@Override
	public int compareTo(final RefLog right) {
		return ComparisonChain.start()
				.compare(right.getCommitter().getWhen(), getCommitter().getWhen())
				.result();
	}
}
