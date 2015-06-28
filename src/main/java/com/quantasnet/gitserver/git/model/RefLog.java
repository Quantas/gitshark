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
	private final String message;
	
	public RefLog(final ReflogEntry reflogEntry, final GitRepository repo, final Repository db) {
		super(null, repo);
		this.reflogEntry = reflogEntry;
		this.message = generateMessage(reflogEntry, db);
	}

	private String generateMessage(final ReflogEntry reflogEntry, final Repository db) {
		
		final StringBuilder output = new StringBuilder();
		
		try (final RevWalk revWalk = new RevWalk(db)) {
			
			final ObjectId newId = reflogEntry.getNewId();
			final ObjectId oldId = reflogEntry.getOldId();
			
			revWalk.markStart(revWalk.parseCommit(newId));
			
			final List<RevCommit> commits = new ArrayList<>();
			
			for (final RevCommit commit : revWalk) {
				if (commit.getId().equals(oldId)) {
					break;
				}
				
				commits.add(commit);
			}
			
			commits.forEach(commit -> {
				output
					.append("commit=")
					.append(commit.getId().getName())
					.append(", ");
			});
		} catch(final IOException io) {
			
		}
		
		return output.toString();
		
	}
	
	@Override
	protected PersonIdent getCommitter() {
		return reflogEntry.getWho();
	}
	
	public String getMessage() {
		return message;
	}

	public String getComment() {
		return reflogEntry.getComment();
	}
}
