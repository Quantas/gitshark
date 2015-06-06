package com.quantasnet.gitserver.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.quantasnet.gitserver.jgit.GitHttpServletResponseFilter;
import com.quantasnet.gitserver.jgit.vendor.EtagHeaderFilter;
import com.quantasnet.gitserver.jgit.vendor.NoCacheFilter;

@Configuration
public class FilterConfig {

private static final List<String> GIT_URL_PATTERNS = Arrays.asList("/repo/*");  
	
	@Bean
	public FilterRegistrationBean etagFilter() {
		final FilterRegistrationBean bean = new FilterRegistrationBean(new EtagHeaderFilter());
		bean.setUrlPatterns(GIT_URL_PATTERNS);
		return bean;
	}
	
	@Bean
	public FilterRegistrationBean cacheControlFilter() {
		final FilterRegistrationBean bean = new FilterRegistrationBean(new NoCacheFilter());
		bean.setUrlPatterns(GIT_URL_PATTERNS);
		return bean;
	}
	
	@Bean
	public FilterRegistrationBean charsetFixServlet() {
		final FilterRegistrationBean bean = new FilterRegistrationBean(new GitHttpServletResponseFilter());
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		bean.setUrlPatterns(GIT_URL_PATTERNS);
		return bean;
	}
	
}
