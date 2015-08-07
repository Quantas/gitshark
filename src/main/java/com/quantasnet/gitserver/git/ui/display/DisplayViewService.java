package com.quantasnet.gitserver.git.ui.display;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DisplayViewService {

	@Autowired
	private List<DisplayView> displayViewsList;

	private Map<DisplayType, DisplayView> displayViews = new HashMap<>();

	@PostConstruct
	protected void postConstruct() {
		for (final DisplayView view : displayViewsList) {
			displayViews.put(view.getType(), view);
		}
	}

	public DisplayView getForType(final DisplayType type) {
		return displayViews.get(type);
	}
}
