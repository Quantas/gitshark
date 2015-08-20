package com.quantasnet.gitshark.git.ui.display;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Convert Strings to {@link DisplayType}s
 */
@Component
public class DisplayTypeConverter implements Converter<String, DisplayType> {

	@Override
	public DisplayType convert(final String source) {
		return DisplayType.getType(source);
	}
}
