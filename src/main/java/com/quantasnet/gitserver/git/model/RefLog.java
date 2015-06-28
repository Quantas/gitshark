package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.lib.CheckoutEntry;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.ReflogEntry;

import com.quantasnet.gitserver.git.repo.GitRepository;

public class RefLog extends BaseCommit {

	private final ReflogEntry refLogEntry;
	
	public RefLog(final ReflogEntry refLogEntry, final GitRepository repo) {
		super(null, repo);
		this.refLogEntry = refLogEntry;
	}

	@Override
	protected PersonIdent getCommitter() {
		return refLogEntry.getWho();
	}

	/**
	 * @return textual description of the change
	 */
	public String getComment() {
		return refLogEntry.getComment();
	}

	/**
	 * @return a {@link CheckoutEntry} with parsed information about a branch
	 *         switch, or null if the entry is not a checkout
	 */
	public CheckoutEntry parseCheckout() {
		return refLogEntry.parseCheckout();
	}
	
}
