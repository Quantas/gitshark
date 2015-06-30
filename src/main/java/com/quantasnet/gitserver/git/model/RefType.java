package com.quantasnet.gitserver.git.model;

import com.quantasnet.gitserver.Constants;

public enum RefType {

	BRANCH("branch", "Branches", Constants.REFS_HEADS),
	TAG("tag", "Tags", Constants.REFS_TAGS);
	
	private static final RefType[] VALUES = values();
	
	final String name;
	final String title;
	final String refs;
	
	private RefType(final String name, final String title, final String refs) {
		this.name = name;
		this.title = title;
		this.refs = refs;
	}
	
	public static RefType getForName(final String name) {
		for (final RefType type : VALUES) {
			if (type.name.equals(name)) {
				return type;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRefs() {
		return refs;
	}
	
	public String getTitle() {
		return title;
	}
}
