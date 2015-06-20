package com.quantasnet.gitserver.git.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.quantasnet.gitserver.git.exception.RepositoryNotFoundException;

@ControllerAdvice(assignableTypes = { SummaryController.class, TreeController.class, CommitsController.class, SettingsController.class, RepoManageController.class })
public class ExceptionAdvice {

	@ExceptionHandler(RepositoryNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String repoNotFound() {
		return "notfound";
	}
	
}
