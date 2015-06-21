package com.quantasnet.gitserver.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.quantasnet.gitserver.git.repo.RepositoryResolver;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Bean
	public RepositoryResolver repositoryResolver() {
		return new RepositoryResolver();
	}
	
	@Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        super.addViewControllers(registry);
        registry.addViewController("/login").setViewName("login");
    }
	
	@Override
	public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
		super.addArgumentResolvers(argumentResolvers);
		argumentResolvers.add(repositoryResolver());
	}
	
}
