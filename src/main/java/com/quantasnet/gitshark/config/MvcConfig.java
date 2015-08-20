package com.quantasnet.gitshark.config;

import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.quantasnet.gitshark.git.service.RepositoryResolver;
import com.quantasnet.gitshark.git.ui.display.DisplayTypeConverter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private RepositoryResolver repositoryResolver;

	@Autowired
	private DisplayTypeConverter displayTypeConverter;
	
	@Override
	public void addViewControllers(final ViewControllerRegistry registry) {
		super.addViewControllers(registry);
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/401").setViewName("error/401");
		registry.addViewController("/403").setViewName("error/403");
		registry.addViewController("/404").setViewName("error/404");
		registry.addViewController("/405").setViewName("error/404");
		registry.addViewController("/503").setViewName("error/503");
	}
	
	/**
	 * See for details: https://github.com/spring-projects/spring-boot/issues/2774
	 *
	 * @param springSecurityFilterChain
	 * @return
	 */
	@Bean
	public FilterRegistrationBean getSpringSecurityFilterChainBindedToError(
			@Qualifier("springSecurityFilterChain") Filter springSecurityFilterChain) {

		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(springSecurityFilterChain);
		registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		return registration;
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return container -> container.addErrorPages(
				new ErrorPage(HttpStatus.UNAUTHORIZED, "/401"),
				new ErrorPage(HttpStatus.FORBIDDEN, "/403"),
				new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
				new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/405"),
				new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/503"));
	}

	@Override
	public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
		super.addArgumentResolvers(argumentResolvers);
		argumentResolvers.add(repositoryResolver);
	}

	@Override
	public void addFormatters(final FormatterRegistry registry) {
		super.addFormatters(registry);
		registry.addConverter(displayTypeConverter);
	}
}
