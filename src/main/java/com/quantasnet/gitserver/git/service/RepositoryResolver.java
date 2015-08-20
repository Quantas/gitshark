package com.quantasnet.gitserver.git.service;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.quantasnet.gitserver.backend.mongo.MongoRepo;
import com.quantasnet.gitserver.backend.mongo.MongoService;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.exception.RepositoryNotFoundException;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;

@Component
public class RepositoryResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private MongoService mongoService;
	
	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isAssignableFrom(GitRepository.class);
	}

	@Override
	public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {

		final Principal principalObject = webRequest.getUserPrincipal();

		User user = null;
		String userName = null;

		if (null != principalObject) {
			user = (User) ((Authentication) principalObject).getPrincipal();
			userName = user.getUserName();
		}

		final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		final String requestURI = request.getServletPath();
		final String owner = requestURI.split("/")[2];
		final String repoName = removeDotGit(requestURI.split("/")[3]);
		
		final MongoRepo mongoRepo = mongoService.getRepo(repoName, owner, userName, user);
		
		if (null == mongoRepo) {
			throw new RepositoryNotFoundException(repoName);
		}
		
		final GitRepository repo = buildRepo(mongoRepo, owner);
		mavContainer.addAttribute("repo", repo);
		mavContainer.addAttribute("checkoutUrl", buildCheckoutUrl(request, userName, repo));

		return repo;
	}
	
	private GitRepository buildRepo(final MongoRepo mongoRepo, final String owner) throws GitServerException {
		final GitRepository repo = new GitRepository(mongoService, mongoRepo, owner, mongoRepo.getName(), false, false);
		repo.setCommits(repoUtils.hasCommits(repo));
		return repo;
	}
	
	private String buildCheckoutUrl(final HttpServletRequest request, final String userName, final GitRepository repo) {
		return request.getScheme() + "://" + userName + '@' + request.getServerName() + ':' + request.getServerPort() + "/repo/"
				+ repo.getOwner() + '/' + repo.getName();
	}
	
	private String removeDotGit(final String name) {
		if (name.endsWith(".git")) {
			return name.substring(0, name.length() - 4);
		}
		return name;
	}
}