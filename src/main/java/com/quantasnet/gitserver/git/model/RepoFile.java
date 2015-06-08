package com.quantasnet.gitserver.git.model;

import com.google.common.collect.ComparisonChain;

public class RepoFile implements Comparable<RepoFile> {

	private final String name;
	private final boolean directory;

	public RepoFile(final String name, final boolean directory) {
		this.name = name;
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public boolean isDirectory() {
		return directory;
	}

	@Override
	public int compareTo(final RepoFile o) {
		return ComparisonChain.start()
			.compareTrueFirst(directory, o.isDirectory())
			.compare(name, o.getName())
			.result();
	}
}