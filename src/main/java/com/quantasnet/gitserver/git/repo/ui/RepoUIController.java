package com.quantasnet.gitserver.git.repo.ui;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.repo.RepositoryService;

@RequestMapping("/repo")
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
	
	@RequestMapping("/{repoOwner}/{repoName}")
	public String displayRepo(@AuthenticationPrincipal final User user, final GitRepository repo, final Model model) throws Exception {

		final Repository db = repo.getDB();
		
		final boolean commits = GitRepository.hasCommits(db);
		
		db.close();
		
		model.addAttribute("repo", repo);
		model.addAttribute("commits", commits);
		
		return "git/single";
	}
}
