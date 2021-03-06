package com.quantasnet.gitshark.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.CachePublicMetrics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.codahale.metrics.MetricRegistry;

@RequestMapping("/admin/metrics")
@Controller
public class MetricsController {

	@Autowired
	private MetricRegistry metricRegistry;
	
	@Autowired
	private CachePublicMetrics cacheMetrics;

	@RequestMapping(method = RequestMethod.GET)
	public String metrics(final Model model) {
		model.addAttribute("meters", metricRegistry.getMeters());
		model.addAttribute("counters", metricRegistry.getCounters());
		model.addAttribute("timers", metricRegistry.getTimers());
		model.addAttribute("caches", cacheMetrics.metrics());
		return "admin/metrics";
	}
}
