package com.quantasnet.gitserver.git.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.html.HtmlEscapers;

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

	public List<HunkLine> getContentsLines() {
		return Arrays.stream(
			contents.split("\n"))
			.map(this::buildLine)
			.collect(Collectors.toList());
	}
	
	public int getAdditions() {
		return additions;
	}
	
	public int getRemovals() {
		return removals;
	}

	private HunkLine buildLine(final String line) {
		final boolean add = line.startsWith("+");
		final boolean delete = line.startsWith("-");

		String newLine = line;
		if (add || delete) {
			newLine = line.substring(1);
		}

		newLine = HtmlEscapers.htmlEscaper().escape(newLine)
			.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

		return new HunkLine(add, delete, newLine);
	}

	private class HunkLine {
		private final boolean add;
		private final boolean delete;
		private final String text;

		public HunkLine(final boolean add, final boolean delete, final String text) {
			this.add = add;
			this.delete = delete;
			this.text = text;
		}

		public boolean isAdd() {
			return add;
		}

		public boolean isDelete() {
			return delete;
		}

		public String getText() {
			return text;
		}
	}
}
