package com.quantasnet.gitserver.admin.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.CachePublicMetrics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.codahale.metrics.MetricRegistry;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

@RequestMapping("/admin/metrics")
@Controller
public class MetricsController {

	@Autowired
	private MetricRegistry metricRegistry;
	
	@Autowired
	private CachePublicMetrics cacheMetrics;

	@RequestMapping(method = RequestMethod.GET)
	public String metrics(final Model model) {
		model.addAttribute("gauges", metricRegistry.getGauges());
		model.addAttribute("meters", metricRegistry.getMeters());
		model.addAttribute("counters", metricRegistry.getCounters());
		model.addAttribute("timers", metricRegistry.getTimers());
		model.addAttribute("caches", cacheMetrics.metrics());
		model.addAttribute("jvm", jvmInfo());
		model.addAttribute("loggers", getLoggers());
		return "admin/metrics";
	}

	private Map<String, String> jvmInfo() {
		final Map<String, String> info = new HashMap<>();
		final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

		info.put("start", new Date(runtimeMXBean.getStartTime()).toString());
		info.put("uptime", DurationFormatUtils.formatDurationWords(runtimeMXBean.getUptime(), true, false));
		info.put("spec.vendor", runtimeMXBean.getSpecVendor());
		info.put("spec.version", runtimeMXBean.getSpecVersion());
		info.put("name", runtimeMXBean.getVmName());

		return info;
	}

	public List<Logger> getLoggers() {
		return ((LoggerContext) LoggerFactory.getILoggerFactory())
				.getLoggerList()
				.stream()
				.filter(logger -> StringUtils.isNotEmpty(logger.getName()))
				.collect(Collectors.toList());
	}
}
