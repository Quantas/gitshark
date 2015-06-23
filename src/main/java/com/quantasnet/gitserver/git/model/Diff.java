package com.quantasnet.gitserver.git.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.springframework.util.StringUtils;

public class Diff {

	private static final String HUNK_HEADER_REGEX = "(?:@@) ([-+]\\d+),(\\d+)\\s([+-]\\d+),(\\d+) (?:@@)";
	
	private static final Pattern HUNK_HEADER = Pattern.compile(HUNK_HEADER_REGEX);
	
	private final List<Hunk> hunks;
	private final String fileName;
	private final ChangeType changeType;
	
	public Diff(final String diffString, final String fileName, final ChangeType changeType) {
		this.hunks = filterDiffString(diffString);
		this.fileName = fileName;
		this.changeType = changeType;
	}

	private List<Hunk> filterDiffString(final String origString) {
		final int index = origString.indexOf("@@");
		if (index > -1) {
			return splitHunks(origString.substring(index));
		}
		return splitHunks(origString);
	}
	
	private List<Hunk> splitHunks(final String diff) {
		final List<String> hunkContents = 
				Arrays.asList(diff.split(HUNK_HEADER_REGEX))
					.stream()
					.filter(s -> !StringUtils.isEmpty(s.trim()))
					.collect(Collectors.toList());
		
		final Matcher matcher = HUNK_HEADER.matcher(diff);

		final List<String> hunkHeaders = new ArrayList<>();
		while (matcher.find()) {
			hunkHeaders.add(matcher.group());
		}
		
		final List<Hunk> hunks = new ArrayList<>();
		
		for (int i = 0; i < hunkContents.size(); i++) {
			if (hunkHeaders.isEmpty()) {
				hunks.add(new Hunk(null, "Empty File Added"));
			} else {
				hunks.add(new Hunk(hunkHeaders.get(i), hunkContents.get(i)));
			}
		}
		
		return hunks;
	}
	
	public int getAdditions() {
		int adds = 0;
		for (final Hunk hunk : hunks) {
			adds += hunk.getAdditions();
		}
		
		return adds;
	}
	
	public int getDeletions() {
		int deletes = 0;
		for (final Hunk hunk : hunks) {
			deletes += hunk.getRemovals();
		}
		
		return deletes;
	}
	
	public List<Hunk> getHunks() {
		return hunks;
	}

	public String getFileName() {
		return fileName;
	}
	
	public ChangeType getChangeType() {
		return changeType;
	}
}
