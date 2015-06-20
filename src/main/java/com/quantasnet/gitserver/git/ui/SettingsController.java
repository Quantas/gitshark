package com.quantasnet.gitserver.git.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class SettingsController {

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public String settings(final GitRepository repo) throws Exception {
		return "git/settings";
	}
	
}
