package com.quantasnet.gitserver.git.repo;

import java.io.File;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.lib.Repository;
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
	private FilesystemRepositoryService repositoryService;
	
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
		
		final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		final String requestURI = request.getServletPath();
		final String owner = requestURI.split("/")[2];
		final String repoName = requestURI.split("/")[3];
		
		final GitRepository repo = repositoryService.getRepository(userName, owner, repoName);
		mavContainer.addAttribute("repo", repo);
		mavContainer.addAttribute("checkoutUrl", buildCheckoutUrl(request, userName, repo));
		
		repo.execute(db -> {
			repo.setHasCommits(hasCommits(db));
			mavContainer.addAttribute("hasCommits", repo.isHasCommits());
		});
		
		return repo;
	}
	
	private String buildCheckoutUrl(final HttpServletRequest request, final String userName, final GitRepository repo) {
		return new StringBuilder()
			.append(request.getScheme())
			.append("://")
			.append(userName)
			.append('@')
			.append(request.getServerName())
			.append(':')
			.append(request.getServerPort())
			.append("/repo/")
			.append(repo.getOwner())
			.append('/')
			.append(repo.getName())
			.toString();
	}
	
	private boolean hasCommits(final Repository repository) {
		if (repository != null && repository.getDirectory().exists()) {
			return (new File(repository.getDirectory(), "objects").list().length > 2)
					|| (new File(repository.getDirectory(), "objects/pack").list().length > 0);
		}
		return false;
	}
}