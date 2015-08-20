package com.quantasnet.gitshark;

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
	
}
