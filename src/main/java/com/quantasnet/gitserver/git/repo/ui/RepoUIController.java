package com.quantasnet.gitserver.git.repo.ui;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitserver.git.repo.RepositoryService;

@RequestMapping("/ui/repo")
@Controller
public class RepoUIController {

	@Autowired
	private RepositoryService repoService;
	
	@RequestMapping
	public String myRepos(@AuthenticationPrincipal final User user, final Model model) {
		model.addAttribute("repos", repoService.getRepositories(user.getUsername()));
		return "git/list";
	}
	
	@RequestMapping("/create/{repoName}")
	public String createRepo(@AuthenticationPrincipal final User user, @PathVariable final String repoName) throws IOException {
		repoService.createRepo(repoName, user.getUsername());
		return "redirect:/ui/repo/";
	}
}
