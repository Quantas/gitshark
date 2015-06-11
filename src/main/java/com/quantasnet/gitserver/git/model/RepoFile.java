package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.joda.time.DateTime;

import com.google.common.collect.ComparisonChain;

public class RepoFile implements Comparable<RepoFile> {

	private final String name;
	private final String display;
	private final String parent;
	private final boolean directory;
	private final long size;
	private final String objectId;
	private final RevCommit commit;
	
	private String fileContents;

	public RepoFile(final String name, final String parent, final boolean directory, final long size, final String objectId, final RevCommit commit) {
		this(name, name, parent, directory, size, objectId, commit);
	}
	
	public RepoFile(final String name, final String display, final String parent, final boolean directory, final long size, final String objectId, final RevCommit commit) {
		this.name = name;
		this.display = display;
		this.parent = parent;
		this.directory = directory;
		this.size = size;
		this.objectId = objectId;
		this.commit = commit;
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
	
	public RevCommit getCommit() {
		return commit;
	}
	
	public String getDateTimeString() {
		if (null != commit) {
			return new DateTime(commit.getCommitterIdent().getWhen().getTime()).toString("yyyy-MM-dd'T'HH:mm:ssZ");
		}
		
		return null;
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