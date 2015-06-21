package com.quantasnet.gitserver.git.model;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class Diff {

	private final String diffString;
	private final String fileName;
	private final ChangeType changeType;
	
	public Diff(final String diffString, final String fileName, final ChangeType changeType) {
		this.diffString = filterDiffString(diffString);
		this.fileName = fileName;
		this.changeType = changeType;
	}

	private String filterDiffString(final String origString) {
		return origString.substring(origString.indexOf("@@"));
	}
	
	public String getDiffString() {
		return diffString;
	}

	public String getFileName() {
		return fileName;
	}
	
	public ChangeType getChangeType() {
		return changeType;
	}
}