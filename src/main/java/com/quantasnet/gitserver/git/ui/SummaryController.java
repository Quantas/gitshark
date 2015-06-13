package com.quantasnet.gitserver.git.ui;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class SummaryController {

	@RequestMapping({ "", "/" })
	public String summary(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model) throws Exception {
		
		model.addAttribute("repo", repo);
		
		GitRepository.execute(repo, db -> {
			model.addAttribute("commits", new Git(db).log().setMaxCount(20).call());
		});
		
		return "git/summary";
		
	}
	
}
