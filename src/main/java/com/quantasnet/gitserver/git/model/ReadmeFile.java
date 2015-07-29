package com.quantasnet.gitserver.git.model;

import java.io.Serializable;

/**
 * Created by andrewlandsverk on 7/25/15.
 */
public class ReadmeFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final String contents;

	public ReadmeFile(final String name, final String contents) {
		this.name = name;
		this.contents = contents;
	}

	public String getName() {
		return name;
	}

	public String getContents() {
		return contents;
	}
}
