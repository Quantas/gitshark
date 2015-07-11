package com.quantasnet.gitserver.git.model;

import java.io.Serializable;
import java.util.Arrays;

public class Hunk implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String header;
	private final String contents;
	
	private int additions;
	private int removals;
	
	public Hunk(final String header, final String contents) {
		this.header = header;
		this.contents = contents;
		calculateChanges(contents);
	}

	private void calculateChanges(final String contents) {
		Arrays.asList(contents.split("\\n")).forEach(s -> {
			if (s.startsWith("+")) {
				additions++;
			} else if (s.startsWith("-")) {
				removals++;
			}
		});
	}
	
	public String getHeader() {
		return header;
	}

	public String getContents() {
		return contents;
	}
	
	public int getAdditions() {
		return additions;
	}
	
	public int getRemovals() {
		return removals;
	}
}
