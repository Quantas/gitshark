package com.quantasnet.gitserver.git.repo;

import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class RepositoryResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isAssignableFrom(GitRepository.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		final Principal principalObject = webRequest.getUserPrincipal();
		String userName = null;
		
		if (null != principalObject) {
			userName = principalObject.getName();
		}
		
		final Set<GitRepository> repos = repositoryService.getRepositories(userName);
		
		final String requestURI = webRequest.getNativeRequest(HttpServletRequest.class).getServletPath();
		
		final String repoName = requestURI.split("/")[3];
		
		for (final GitRepository repo : repos) {
			if (repo.getName().equals(repoName)) {
				return repo;
			}
		}
		
		return null;
	}

}
