package com.quantasnet.gitserver.config;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.quantasnet.gitserver.git.protocol.http.GitHttpServletResponseFilter;

@Configuration
public class FilterConfig {
	
	@Bean
	public FilterRegistrationBean charsetFixServlet() {
		final FilterRegistrationBean bean = new FilterRegistrationBean(new GitHttpServletResponseFilter());
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		bean.addUrlPatterns("/repo/*");
		return bean;
	}
	
}
