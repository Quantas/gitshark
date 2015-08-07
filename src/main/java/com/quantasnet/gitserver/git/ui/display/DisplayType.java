package com.quantasnet.gitserver.git.ui.display;

/**
 * Type of repo view to display
 */
public enum DisplayType {
	TREE("tree"),
	FILE("file"),
	HISTORY("history"),
	RAW("raw");

	private static final DisplayType[] VALUES = values();

	private final String type;

	DisplayType(final String type) {
		this.type = type;
	}

	/**
	 * @return String representation of {@link DisplayType}
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type String representation of DisplayType
	 * @return {@link DisplayType} for the String value
	 */
	public static DisplayType getType(final String type) {
		for (final DisplayType displayType : VALUES) {
			if (displayType.getType().equals(type)) {
				return displayType;
			}
		}
		return null;
	}
}
