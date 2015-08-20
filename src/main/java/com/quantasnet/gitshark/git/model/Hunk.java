package com.quantasnet.gitshark.git.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.html.HtmlEscapers;

public class Hunk implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String END_OF_FILE = "\\ No newline at end of file";

	private static final String NEW_LINE = "\\n";
	private static final String TAB = "\\t";

	private static final String ADD = "+";
	private static final String REMOVE = "-";

	private static final Pattern HEADER_PATTERN = Pattern.compile(Diff.HUNK_HEADER_REGEX);

	private final String header;
	private final int startLine;
	private final String contents;
	
	private int additions;
	private int removals;
	
	public Hunk(final String header, final String contents) {
		this.header = header;
		this.startLine = determineStartLine(header);
		this.contents = contents;
		calculateChanges(contents);
	}

	private int determineStartLine(final String header) {
		if (null == header) {
			return 1;
		}

		final Matcher matcher = HEADER_PATTERN.matcher(header);
		if (matcher.find()) {
			return Math.abs(Integer.valueOf(matcher.group(1)));
		}
		return 1;
	}

	private void calculateChanges(final String contents) {
		Arrays.asList(contents.split(NEW_LINE)).forEach(s -> {
			if (s.startsWith(ADD)) {
				additions++;
			} else if (s.startsWith(REMOVE)) {
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
		final List<HunkLine> returnList = new ArrayList<>();

		int currentLineRight = startLine - 1;
		int currentLineLeft = startLine - 1;

		for (final String  line : contents.replaceFirst(NEW_LINE, "").split(NEW_LINE)) {
			final boolean add = line.startsWith(ADD);
			final boolean delete = line.startsWith(REMOVE);

			boolean leftNull = false;
			boolean rightNull = false;

			String newLine = line;
			if (add || delete) {
				newLine = line.substring(1);

				if (add) {
					currentLineRight++;
					leftNull = true;
				} else {
					currentLineLeft++;
					rightNull = true;
				}
			} else if (END_OF_FILE.equals(line) || Diff.EMPTY_HUNK_HEADER.equals(line)) {
				leftNull = true;
				rightNull = true;
			} else {
				currentLineLeft++;
				currentLineRight++;
			}

			newLine = HtmlEscapers.htmlEscaper().escape(newLine)
				.replaceAll(TAB, "&nbsp;&nbsp;&nbsp;&nbsp;");

			returnList.add(new HunkLine(add, delete, leftNull ? null : currentLineLeft, rightNull ? null : currentLineRight, newLine));
		}

		return returnList;
	}
	
	public int getAdditions() {
		return additions;
	}
	
	public int getRemovals() {
		return removals;
	}

	public int getStartLine() {
		return startLine;
	}

	public class HunkLine implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final boolean add;
		private final boolean delete;
		private final Integer leftNumber;
		private final Integer rightNumber;
		private final String text;

		public HunkLine(final boolean add, final boolean delete, final Integer leftNumber, final Integer rightNumber, final String text) {
			this.add = add;
			this.delete = delete;
			this.leftNumber = leftNumber;
			this.rightNumber = rightNumber;
			this.text = text;
		}

		public boolean isAdd() {
			return add;
		}

		public boolean isDelete() {
			return delete;
		}

		public Integer getLeftNumber() {
			return leftNumber;
		}

		public Integer getRightNumber() {
			return rightNumber;
		}

		public String getText() {
			return text;
		}
	}
}
