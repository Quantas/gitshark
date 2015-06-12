package com.quantasnet.gitserver.git.model;

import java.util.ArrayList;
import java.util.List;

public class Breadcrumb {

	private final boolean first;
	private final String name;
	private final String url;
	private final boolean link;
	
	private Breadcrumb(boolean first, String name, String url, boolean link) {
		this.first = first;
		this.name = name;
		this.url = url;
		this.link = link;
	}

	public static List<Breadcrumb> generateBreadcrumbs(final String contextPath, final String root, final String path) {
		final List<Breadcrumb> breadcrumbs = new ArrayList<>();
		final String[] paths = path.split("/");
		final int length = paths.length;
		
		String previous = null;
		
		breadcrumbs.add(new Breadcrumb(true, "/", root, true));
		
		if (path.length() > 1) {
			for (int i = 0; i < length; i++) {
				final String chunk = paths[i];
				
				if (null == previous) {
					previous = chunk;
				} else {
					previous += "/" + chunk;
				}
				
				breadcrumbs.add(new Breadcrumb(false, chunk, contextPath + root + previous, i != length - 1));
			}
		}
		
		return breadcrumbs;
	}
	
	public boolean isFirst() {
		return first;
	}
	
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
	
	public boolean isLink() {
		return link;
	}
}