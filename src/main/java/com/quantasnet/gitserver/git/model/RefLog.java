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

import com.quantasnet.gitserver.git.repo.GitRepository;

public class RefLog extends BaseCommit {

	private final ReflogEntry reflogEntry;
	private final List<Commit> commits;
	
	public RefLog(final ReflogEntry reflogEntry, final GitRepository repo, final Repository db) {
		super(null, repo);
		this.reflogEntry = reflogEntry;
		this.commits = generateCommits(reflogEntry, db, repo);
	}

	private List<Commit> generateCommits(final ReflogEntry reflogEntry, final Repository db, final GitRepository repo) {
		
		final List<Commit> commits = new ArrayList<>();
		
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
		} catch(final IOException io) {
			
		}
		
		return commits;
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
}
