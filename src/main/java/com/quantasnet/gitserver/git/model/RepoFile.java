package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class RepoFile extends BaseCommit implements Comparable<RepoFile> {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final String display;
	private final String parent;
	private final boolean directory;
	private final String branch;
	private final String objectId;
	private final String url;
	
	private byte[] fileContentsRaw;

	public RepoFile(final GitRepository repo, final String name, final String parent, final boolean directory, final String branch, final String objectId, final RevCommit commit) {
		this(repo, name, name, parent, directory, branch, objectId, commit);
	}
	
	public RepoFile(final GitRepository repo, final String name, final String display, final String parent, final boolean directory, final String branch, final String objectId, final RevCommit commit) {
		super(commit, repo);
		this.name = name;
		this.display = display;
		this.parent = parent;
		this.directory = directory;
		this.branch = branch;
		this.objectId = objectId;
		this.url = generateUrl(repo);
	}

	@Override
	protected PersonIdent getCommitter() {
		return commit.getCommitterIdent();
	}

	@Override
	protected PersonIdent getAuthor() {
		return commit.getAuthorIdent();
	}

	private String generateUrl(final GitRepository repo) {
		final StringBuilder builder = new StringBuilder();
		
		builder.append(repo.getOwner())
			.append('/')
			.append(repo.getDisplayName())
			.append("/tree/")
			.append(branch)
			.append('/')
			.append(parent)
			.append(name);
		
		if (!directory) {
			builder.append("?file=true");
		}
		
		return builder.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public String getParent() {
		return parent;
	}
	
	public boolean isDirectory() {
		return directory;
	}
	
	public String getObjectId() {
		return objectId;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setFileContentsRaw(byte[] fileContentsRaw) {
		this.fileContentsRaw = fileContentsRaw;
	}
	
	public byte[] getFileContentsRaw() {
		return fileContentsRaw;
	}
	
	public String getFileContents() {
		return new String(fileContentsRaw);
	}
	
	@Override
	public int compareTo(final RepoFile o) {
		return ComparisonChain.start()
			.compareTrueFirst(directory, o.isDirectory())
			.compare(name, o.getName())
			.result();
	}
}