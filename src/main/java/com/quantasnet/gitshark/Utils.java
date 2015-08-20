package com.quantasnet.gitshark;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	private Utils() {
		// no instances
	}
	
	public static <T> List<T> safeguard(final List<T> list) {
		if (null == list) {
			return new ArrayList<>();
		}
		return list;
	}
	
	public static String readableFileSize(final long size) {
		if (size <= 0) {
			return "0";
		}

		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
}
