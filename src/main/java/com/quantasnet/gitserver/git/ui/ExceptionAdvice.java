package com.quantasnet.gitserver.git.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;

@ControllerAdvice("com.quantasnet.gitserver.git.ui")
public class ExceptionAdvice {

	@ExceptionHandler({ GitServerException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String notFound() {
		return "forward:/404";
	}
	
	@ExceptionHandler({ GitServerErrorException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String serverError() {
		return "forward:/503";
	}
}
