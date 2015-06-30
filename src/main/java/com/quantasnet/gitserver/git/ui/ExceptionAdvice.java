package com.quantasnet.gitserver.git.ui;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.quantasnet.gitserver.git.exception.RepositoryNotFoundException;

@ControllerAdvice("com.quantasnet.gitserver.git.ui")
public class ExceptionAdvice {

	@ExceptionHandler({ RepositoryNotFoundException.class, IncorrectObjectTypeException.class, IllegalArgumentException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String repoNotFound() {
		return "forward:/404";
	}
	
}
