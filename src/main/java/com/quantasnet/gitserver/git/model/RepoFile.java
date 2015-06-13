package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.ComparisonChain;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author andrewlandsverk
 */
public class RepoFile extends BaseCommit implements Comparable<RepoFile> {

	private final String name;
	private final String display;
	private final String parent;
	private final boolean directory;
	private final long size;
	private final String objectId;
	private final String url;
	
	private String fileContents;

	public RepoFile(final GitRepository repo, final String name, final String parent, final boolean directory, final long size, final String objectId, final RevCommit commit) {
		this(repo, name, name, parent, directory, size, objectId, commit);
	}
	
	public RepoFile(final GitRepository repo, final String name, final String display, final String parent, final boolean directory, final long size, final String objectId, final RevCommit commit) {
		super(commit);
		this.name = name;
		this.display = display;
		this.parent = parent;
		this.directory = directory;
		this.size = size;
		this.objectId = objectId;
		this.url = generateUrl(repo);
	}

	private String generateUrl(final GitRepository repo) {
		final StringBuilder builder = new StringBuilder();
		
		builder.append(repo.getOwner())
			.append('/')
			.append(repo.getDisplayName())
			.append("/tree/")
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
	
	public long getSize() {
		return size;
	}
	
	public String getObjectId() {
		return objectId;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setFileContents(String fileContents) {
		this.fileContents = fileContents;
	}
	
	public String getFileContents() {
		return fileContents;
	}
	
	@Override
	public int compareTo(final RepoFile o) {
		return ComparisonChain.start()
			.compareTrueFirst(directory, o.isDirectory())
			.compare(name, o.getName())
			.result();
	}
}