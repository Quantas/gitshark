package com.quantasnet.gitserver.git.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.quantasnet.gitserver.git.exception.GitSharkErrorException;
import com.quantasnet.gitserver.git.exception.GitSharkException;
import com.quantasnet.gitserver.git.exception.RepositoryAccessDeniedException;

@ControllerAdvice("com.quantasnet.gitserver.git.ui")
public class ExceptionAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionAdvice.class);

	@ExceptionHandler(RepositoryAccessDeniedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public String accessDenied() {
		return "forward:/401";
	}

	@ExceptionHandler(GitSharkException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String notFound() {
		return "forward:/404";
	}
	
	@ExceptionHandler({ GitSharkErrorException.class, Exception.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String serverError(final Exception exception) {
		LOG.error("Error loading page", exception);
		return "forward:/503";
	}
}
