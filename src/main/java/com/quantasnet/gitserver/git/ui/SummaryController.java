package com.quantasnet.gitserver.git.ui;

import com.quantasnet.gitserver.git.service.ReadmeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class SummaryController {

	@Autowired
	private ReadmeFileService readmeService;
	
	@RequestMapping({ "", "/" })
	public String summary(final GitRepository repo, final Model model) throws GitServerException {
		repo.execute(db -> {
			if (repo.hasCommits()) {
				model.addAttribute("readme", readmeService.resolveReadMeFile(repo, db, db.getBranch()));
			}
		});
		
		return "git/summary";
	}
}