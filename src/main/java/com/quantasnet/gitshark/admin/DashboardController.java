package com.quantasnet.gitshark.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by andrewlandsverk on 7/13/15.
 */
@Controller
@RequestMapping("/admin")
public class DashboardController {

	@Autowired
	private MetricRegistry metricRegistry;

	@RequestMapping(method = RequestMethod.GET)
	public String dashboard(final Model model) {
		model.addAttribute("gauges", metricRegistry.getGauges());
		model.addAttribute("jvm", jvmInfo());
		return "admin/dashboard";
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

}
