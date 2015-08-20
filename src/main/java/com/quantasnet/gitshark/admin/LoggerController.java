package com.quantasnet.gitshark.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * Created by andrewlandsverk on 7/13/15.
 */
@Controller
@RequestMapping("/admin/loggers")
public class LoggerController {

	@RequestMapping(method = RequestMethod.GET)
	public String loggers(final Model model) {
		model.addAttribute("loggers", getLoggers());
		return "admin/loggers";
	}

	private List<Logger> getLoggers() {
		return ((LoggerContext) LoggerFactory.getILoggerFactory())
				.getLoggerList()
				.stream()
				.filter(logger -> StringUtils.isNotEmpty(logger.getName()))
				.collect(Collectors.toList());
	}
}
