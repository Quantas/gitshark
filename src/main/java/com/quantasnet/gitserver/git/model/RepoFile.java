package com.quantasnet.gitserver.git.model;

import com.google.common.collect.ComparisonChain;

public class RepoFile implements Comparable<RepoFile> {

	private final String name;
	private final boolean directory;
	private final long size;
	private final String objectId;

	public RepoFile(final String name, final boolean directory, final long size, final String objectId) {
		this.name = name;
		this.directory = directory;
		this.size = size;
		this.objectId = objectId;
	}

	public String getName() {
		return name;
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

	@Override
	public int compareTo(final RepoFile o) {
		return ComparisonChain.start()
			.compareTrueFirst(directory, o.isDirectory())
			.compare(name, o.getName())
			.result();
	}
}