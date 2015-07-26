package com.quantasnet.gitserver.config;

import java.util.EnumSet;

import javax.annotation.PostConstruct;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlet.InstrumentedFilter;

@Configuration
public class MetricsConfig implements ServletContextInitializer {

	@Autowired
	private MetricRegistry metricRegistry;
	
	@PostConstruct
    public void init() {
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
    }

	@Override
	public void onStartup(final ServletContext servletContext) throws ServletException {
		servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricRegistry);
		
		final FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("metricsFilter", new InstrumentedFilter());
        metricsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC), true, "/*");
        metricsFilter.setAsyncSupported(true);
	}
}
